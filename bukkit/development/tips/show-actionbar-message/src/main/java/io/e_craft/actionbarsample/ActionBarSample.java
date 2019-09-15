package io.e_craft.actionbarsample;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ActionBarSample extends JavaPlugin {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // プレイヤーにしか使えないので、onCommandで使う場合はコンソールかチェックしましょう
        if(!(sender instanceof Player)) {
            sender.sendMessage("Player only");
            return true;
        }

        // 本当はCommandSenderでも送れますが、例示のためにPlayerで
        Player player = (Player)sender;
        // 送信したいメッセージ
        String message = "ActionBar!!";

        // テスト用にメッセージを変更可能に
        if(args.length != 0) {
            message = String.join(" ",args);
        }

        // Stringでは送れません、TextComponentを作成しましょう
        TextComponent component = new TextComponent();
        component.setText(message);
        // 色コードを使用する場合はfromLegacyTextを使用しましょう
        // ちなみに、fromLegacyTextが返す値はBaseComponent[](BaseComponentの配列)なので要注意
        //BaseComponent[] component = TextComponent.fromLegacyText(message);

        // アクションバーのメッセージ送信はコレ！
        // ChatMessageType.ACTION_BARを指定しましょう
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,component);

        // ちなみに、タイトルの送信はこれを使用します
        //player.sendTitle("Title","Subtitle",10,70,20);
        return true;
    }
}
