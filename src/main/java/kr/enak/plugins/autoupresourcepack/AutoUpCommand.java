package kr.enak.plugins.autoupresourcepack;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AutoUpCommand implements CommandExecutor {
    public static String url = "";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(String.format("/%s <check|force|suggest>", label));
            return true;
        }

        if (args[0].equalsIgnoreCase("suggest")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("플레이어만 사용할 수 있는 명령어입니다");
                return false;
            }

            ((Player) sender).setResourcePack(url);
            return true;
        } else if (args[0].equalsIgnoreCase("check") || args[0].equalsIgnoreCase("force")) {
            Bukkit.getScheduler().runTaskAsynchronously(AutoUpResourcePack.INSTANCE,
                    () -> AutoUpResourcePack.INSTANCE.rawUpdateChecker(args[0].equalsIgnoreCase("force")));
            return true;
        }

        sender.sendMessage(String.format("/%s <check|suggest>", label));
        return true;
    }
}
