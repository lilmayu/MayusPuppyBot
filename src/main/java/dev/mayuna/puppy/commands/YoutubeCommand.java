package dev.mayuna.puppy.commands;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.downloader.YoutubeProgressCallback;
import com.github.kiulian.downloader.downloader.request.RequestVideoFileDownload;
import com.github.kiulian.downloader.downloader.request.RequestVideoInfo;
import com.github.kiulian.downloader.downloader.response.Response;
import com.github.kiulian.downloader.model.videos.VideoDetails;
import com.github.kiulian.downloader.model.videos.VideoInfo;
import com.github.kiulian.downloader.model.videos.formats.Format;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import dev.mayuna.mayusjdautils.arguments.ArgumentParser;
import dev.mayuna.mayusjdautils.utils.DiscordUtils;
import dev.mayuna.mayusjdautils.utils.MessageInfo;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YoutubeCommand {

    // Data
    private static final String FOLDER_PATH = "./temp/yt/";
    private static final String VIDEO_REGEX = "https?://(?:www\\.)?youtu(?:\\.be/|be\\.com/(?:watch\\?v=|v/|embed/|user/(?:[\\w#]+/)+))([^&#?\\n]+)";

    // Objects
    private static final YoutubeDownloader youtubeDownloader = new YoutubeDownloader();

    public static void register(CommandClientBuilder client) {
        client.addSlashCommands(new YoutubeSlashCommand());
        client.addCommands(new YoutubePrefixCommand());
    }

    private static void process(YoutubeRequest youtubeRequest) {
        if (youtubeRequest.url == null) {
            sendError("Invalid syntax! You have to specified at least valid YouTube link.", youtubeRequest);
            return;
        }

        if (youtubeRequest.format == null) {
            youtubeRequest.setFormat("audio");
        }

        if (!youtubeRequest.parse()) {
            sendError("You have entered invalid YouTube URL!", youtubeRequest);
            return;
        }

        sendInformation("Loading information about video with ID `" + youtubeRequest.videoId + "`...", youtubeRequest);

        RequestVideoInfo requestVideoInfo = new RequestVideoInfo(youtubeRequest.videoId);
        Response<VideoInfo> videoInfoResponse = youtubeDownloader.getVideoInfo(requestVideoInfo);
        if (!videoInfoResponse.ok()) {
            sendError("Cannot load video with ID `" + youtubeRequest.videoId + "`!", youtubeRequest);
            return;
        }

        VideoInfo videoInfo = videoInfoResponse.data();
        VideoDetails videoDetails = videoInfo.details();
        Format format;

        if (!videoDetails.isDownloadable()) {
            sendError("Cannot download video with ID `" + youtubeRequest.videoId + "`!", youtubeRequest);
            return;
        }

        if (videoDetails.lengthSeconds() >= 3600) {
            sendError("Cannot download videos longer 1 hour. Fix coming soon :tm:", youtubeRequest);
            return;
        }

        if (youtubeRequest.format.equalsIgnoreCase("audio")) {
            format = videoInfo.bestAudioFormat();
        } else {
            format = videoInfo.bestVideoFormat();
        }
        if (format == null) {
            sendError("No available format was found within video with ID `" + youtubeRequest.videoId + "`.", youtubeRequest);
            return;
        }

        AtomicLong lastUpdate = new AtomicLong(System.currentTimeMillis());
        var requestVideoFileDownload = new RequestVideoFileDownload(format).saveTo(new File(FOLDER_PATH)).renameTo(videoDetails.title()).callback(new YoutubeProgressCallback<>() {
            @Override
            public void onDownloading(int progress) {
                if (lastUpdate.get() + 1000 <= System.currentTimeMillis()) {
                    lastUpdate.set(System.currentTimeMillis());
                    sendProgress(progress, videoDetails, youtubeRequest);
                }
            }

            @Override
            public void onFinished(File file) {
                if (file.length() < 8388246) {
                    sendSuccess("Successfully downloaded **" + videoDetails.title() + "** (`" + videoDetails.videoId() + "`)! Sending file...", youtubeRequest);
                    sendFile(file, youtubeRequest);
                } else {
                    sendError("Downloaded video **" + videoInfo.details().title() + "** was (`" + videoDetails.videoId() + "`) was too large to send. Sorry!", youtubeRequest);
                    file.delete();
                }

                System.gc();
            }

            @Override
            public void onError(Throwable throwable) {
                sendException(videoDetails, throwable, youtubeRequest);
            }
        });
        youtubeDownloader.downloadVideoFile(requestVideoFileDownload);
    }

    private static void sendInformation(String content, YoutubeRequest youtubeRequest) {
        sendEx(MessageInfo.Builder.create().setType(MessageInfo.Type.INFORMATION).setEmbed(true).setContent(content), youtubeRequest.message, youtubeRequest.hook);
    }

    private static void sendProgress(int progress, VideoDetails videoDetails, YoutubeRequest youtubeRequest) {
        String content = "";
        content += "Video: **" + videoDetails.title() + "** (`" + videoDetails.videoId() + "`)\n";
        content += "Progress: **" + progress + "%**";

        MessageInfo.Builder builder = MessageInfo.Builder.create().setType(MessageInfo.Type.INFORMATION).setEmbed(true).setContent(content).setCustomTitle("Download Progress");

        sendEx(builder, youtubeRequest.message, youtubeRequest.hook);
    }

    private static void sendError(String content, YoutubeRequest youtubeRequest) {
        sendEx(MessageInfo.Builder.create().setType(MessageInfo.Type.ERROR).setEmbed(true).setContent(content), youtubeRequest.message, youtubeRequest.hook);
    }

    private static void sendException(VideoDetails videoDetails, Throwable throwable, YoutubeRequest youtubeRequest) {
        MessageInfo.Builder builder = MessageInfo.Builder.create()
                .setType(MessageInfo.Type.ERROR)
                .setEmbed(true)
                .setContent("There was an error while downloading **" + videoDetails.title() + "(`" + videoDetails.videoId() + "`)!")
                .addCustomField(new MessageEmbed.Field("Technical details", MessageInfo.formatExceptionInformationField(throwable), false));

        sendEx(builder, youtubeRequest.message, youtubeRequest.hook);

    }

    private static void sendSuccess(String content, YoutubeRequest youtubeRequest) {
        sendEx(MessageInfo.Builder.create().setType(MessageInfo.Type.SUCCESS).setEmbed(true).setContent(content).setClosable(true), youtubeRequest.message, youtubeRequest.hook);
    }

    private static void sendFile(File file, YoutubeRequest youtubeRequest) {
        MessageChannel messageChannel = null;
        if (youtubeRequest.message != null) {
            messageChannel = youtubeRequest.message.getChannel();
        } else if (youtubeRequest.hook != null) {
            messageChannel = youtubeRequest.hook.getInteraction().getMessageChannel();
        }

        if (messageChannel != null) {
            String filename = file.getName();

            if (filename.contains(".")) {
                filename = filename.substring(0, filename.lastIndexOf("."));
            }

            messageChannel.sendFile(file).content(filename).setActionRow(DiscordUtils.generateCloseButton(ButtonStyle.DANGER)).queue(success -> {
                file.delete();
            });
        }
    }

    private static void sendEx(MessageInfo.Builder builder, Message message, InteractionHook hook) {
        if (message != null) {
            builder.edit(message);
        } else if (hook != null) {
            builder.send(hook);
        }
    }

    private static class YoutubePrefixCommand extends Command {

        public YoutubePrefixCommand() {
            this.name = "youtube";
            this.aliases = new String[]{"yt"};

            this.guildOnly = false;

            this.cooldown = 3;
            this.cooldownScope = CooldownScope.GLOBAL;
        }

        @Override
        protected void execute(CommandEvent commandEvent) {
            Message message = commandEvent.getMessage().replyEmbeds(DiscordUtils.getDefaultEmbed().build()).complete();
            ArgumentParser argumentParser = new ArgumentParser(commandEvent.getArgs());

            String url = null, format = null;

            if (argumentParser.hasArgumentAtIndex(0)) {
                url = argumentParser.getArgumentAtIndex(0).getValue();
            }
            if (argumentParser.hasArgumentAtIndex(1)) {
                format = argumentParser.getArgumentAtIndex(1).getValue();
            }

            process(new YoutubeRequest(url, format, message));
        }
    }

    private static class YoutubeSlashCommand extends SlashCommand {

        public YoutubeSlashCommand() {
            this.name = "youtube";

            this.guildOnly = false;

            List<OptionData> options = new ArrayList<>();
            options.add(new OptionData(OptionType.STRING, "url", "URL link to YouTube video", true));
            options.add(new OptionData(OptionType.STRING, "format", "Format in which it will convert to", false).addChoice("Audio", "audio").addChoice("Video", "video"));
            this.options = options;

            this.cooldown = 3;
            this.cooldownScope = CooldownScope.GLOBAL;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            event.deferReply(false).complete();
            InteractionHook hook = event.getHook();

            String format = null, url = event.getOption("url").getAsString();
            if (event.getOption("format") != null) {
                format = event.getOption("format").getAsString();
            }

            process(new YoutubeRequest(url, format, hook));
        }
    }

    private static class YoutubeRequest {

        private @Getter @Setter String url;
        private @Getter @Setter String format;

        private @Getter @Setter Message message;
        private @Getter @Setter InteractionHook hook;

        private @Getter String videoId;

        public YoutubeRequest(String url, String format, Message message) {
            this.url = url;
            this.format = format;
            this.message = message;
        }

        public YoutubeRequest(String url, String format, InteractionHook hook) {
            this.url = url;
            this.format = format;
            this.hook = hook;
        }

        public boolean parse() {
            Matcher matcher = Pattern.compile(VIDEO_REGEX, Pattern.CASE_INSENSITIVE).matcher(url);
            if (!matcher.find()) {
                return false;
            }

            videoId = matcher.group(1);
            return true;
        }

    }
}
