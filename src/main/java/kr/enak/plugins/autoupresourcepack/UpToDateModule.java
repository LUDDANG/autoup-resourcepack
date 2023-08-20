package kr.enak.plugins.autoupresourcepack;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.logging.Level;


public class UpToDateModule {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);

    private long lastModifiedTimestamp;

    private String rawUrl;

    public UpToDateModule(FileConfiguration config) {
        this.lastModifiedTimestamp = config.getLong("lastModifiedTimestamp");
        this.rawUrl = config.getString("url");
    }

    public boolean isPackUpdated(boolean force) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpHead request = new HttpHead(this.rawUrl);

            try (CloseableHttpResponse response = client.execute(request)) {
                Header header = response.getFirstHeader("last-modified");
                String lastModified = header.getValue();

                long epoch = dateFormat.parse(lastModified).getTime();
                boolean flag = force || epoch > lastModifiedTimestamp;
                if (flag) {
                    AutoUpResourcePack.LOGGER.info(String.format("Found updated resourcepack (%d -> %d)", lastModifiedTimestamp, epoch));
                    lastModifiedTimestamp = epoch;
                }
                return flag;
            }
        } catch (Throwable ex) {
            AutoUpResourcePack.LOGGER.log(Level.WARNING, "Cannot retrieve resourcepack from the server", ex);
            return false;
        }
    }

    public void notifyPlayers() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            AutoUpCommand.url = this.rawUrl + String.format("?%s", this.lastModifiedTimestamp);

            BaseComponent[] clickableMessage = new ComponentBuilder(" [여기] ")
                    .color(ChatColor.AQUA)
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/autoup suggest"))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("클릭하여 리소스팩 적용하기")))
                    .create();

            BaseComponent[] message = new ComponentBuilder("[AutoUp] 새로운 버전의 리소스팩을 발견했습니다.")
                    .color(ChatColor.WHITE)
                    .append(clickableMessage)
                    .append(new ComponentBuilder("를 눌러 업데이트해주세요.").color(ChatColor.WHITE).create())
                    .create();

            player.spigot().sendMessage(message);
        });
    }

    public long getLastModifiedTimestamp() {
        return lastModifiedTimestamp;
    }
}
