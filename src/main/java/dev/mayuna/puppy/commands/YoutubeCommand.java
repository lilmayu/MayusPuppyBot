package dev.mayuna.puppy.commands;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.downloader.YoutubeProgressCallback;
import com.github.kiulian.downloader.downloader.request.RequestVideoFileDownload;
import com.github.kiulian.downloader.downloader.request.RequestVideoInfo;
import com.github.kiulian.downloader.downloader.response.Response;
import com.github.kiulian.downloader.model.videos.VideoInfo;
import com.github.kiulian.downloader.model.videos.formats.Format;
import com.jagrosh.jdautilities.command.SlashCommand;
import dev.mayuna.mayusjdautils.utils.DiscordUtils;
import dev.mayuna.mayusjdautils.utils.MessageInfo;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YoutubeCommand extends SlashCommand {

    // Data
    private static final String FOLDER_PATH = "./temp/yt/";
    private static final String VIDEO_REGEX = "https?://(?:www\\.)?youtu(?:\\.be/|be\\.com/(?:watch\\?v=|v/|embed/|user/(?:[\\w#]+/)+))([^&#?\\n]+)";

    // Objects
    private static final YoutubeDownloader youtubeDownloader = new YoutubeDownloader();

    public YoutubeCommand() {
        this.name = "youtube";

        this.guildOnly = false;

        List<OptionData> options = new ArrayList<>();
        options.add(new OptionData(OptionType.STRING, "url", "URL link to YouTube video", true));
        options.add(new OptionData(OptionType.STRING, "format", "Format in which it will convert to", false).addChoice("Audio", "audio").addChoice("Video", "video"));
        this.options = options;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.deferReply(false).complete();
        InteractionHook hook = event.getHook();

        String videoId, fullURL = event.getOption("url").getAsString();
        String formatName = "audio";
        if (event.getOption("format") != null) {
            formatName = event.getOption("format").getAsString();
        }

        Matcher matcher = Pattern.compile(VIDEO_REGEX, Pattern.CASE_INSENSITIVE).matcher(fullURL);
        if (!matcher.find()) {
            sendError("You have entered invalid YouTube URL!", hook);
            return;
        }

        if (!(formatName.equalsIgnoreCase("video") || formatName.equalsIgnoreCase("audio"))) {
            sendError("Invalid format! Please, select `video` or `audio`.", hook);
            return;
        }

        videoId = matcher.group(1);
        sendInfo("Loading information about video with ID `" + videoId + "`...", hook);

        RequestVideoInfo requestVideoInfo = new RequestVideoInfo(videoId);
        Response<VideoInfo> videoInfoResponse = youtubeDownloader.getVideoInfo(requestVideoInfo);
        if (!videoInfoResponse.ok()) {
            sendError("Cannot load video with ID `" + videoId + "`!", hook);
            return;
        }

        VideoInfo videoInfo = videoInfoResponse.data();
        if (!videoInfo.details().isDownloadable()) {
            sendError("Video with ID `" + videoId + "` is not downloadable.", hook);
            return;
        }

        if (videoInfo.details().lengthSeconds() >= 3600) {
            sendError("Cannot download videos longer 1 hour. Even if so, it would be too large to send.", hook);
            return;
        }

        Format format;
        if (formatName.equalsIgnoreCase("video")) {
            format = videoInfo.bestVideoFormat();
        } else {
            format = videoInfo.bestAudioFormat();
        }

        final long[] lastTimeUpdating = {System.currentTimeMillis()};
        RequestVideoFileDownload videoFileDownload = new RequestVideoFileDownload(format).saveTo(new File(FOLDER_PATH))
                .renameTo(videoInfo.details().title())
                .callback(new YoutubeProgressCallback<>() {
                    @Override
                    public void onDownloading(int progress) {
                        if (lastTimeUpdating[0] + 1000 <= System.currentTimeMillis()) {
                            lastTimeUpdating[0] = System.currentTimeMillis();
                            sendInfo("Title: **" + videoInfo.details().title() + "**\nProgress: **" + progress + "%**", hook);
                        }
                    }

                    @Override
                    public void onFinished(File file) {
                        if (file.length() < 8388246) {
                            sendSuccess("Successfully downloaded **" + videoInfo.details().title() + "**! Sending file...", hook);
                            sendFile(file, event.getMessageChannel());
                        } else {
                            sendError("Downloaded file was too large to send. Sorry!", hook);
                        }

                        file.delete();
                        System.gc();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        sendError("There was error while downloading video **" + videoInfo.details().title() + "** (`" + videoInfo.details()
                                .videoId() + "`)!\nTechnical details: `" + throwable.toString() + "`", hook);
                    }
                });
        youtubeDownloader.downloadVideoFile(videoFileDownload);
    }

    private String getExtension(String format) {
        if (format.equalsIgnoreCase("video")) {
            return "mp4";
        } else {
            return "mp3";
        }
    }

    private void sendInfo(String content, InteractionHook hook) {
        MessageInfo.Builder.create().setType(MessageInfo.Type.INFORMATION).setEmbed(true).setContent(content).send(hook);
    }

    private void sendError(String content, InteractionHook hook) {
        MessageInfo.Builder.create()
                .setType(MessageInfo.Type.ERROR)
                .setEmbed(true)
                .setContent(content)
                .setClosable(true)
                .addOnInteractionWhitelist(hook.getInteraction().getUser())
                .send(hook);
    }

    private void sendSuccess(String content, InteractionHook hook) {
        MessageInfo.Builder.create()
                .setType(MessageInfo.Type.SUCCESS)
                .setEmbed(true)
                .setContent(content)
                .setClosable(true)
                .addOnInteractionWhitelist(hook.getInteraction().getUser())
                .send(hook);
    }

    private void sendFile(File file, MessageChannel messageChannel) {
        messageChannel.sendFile(file).setActionRow(DiscordUtils.generateCloseButton(ButtonStyle.DANGER)).queue(success -> {
            file.delete();
        });
    }
}
