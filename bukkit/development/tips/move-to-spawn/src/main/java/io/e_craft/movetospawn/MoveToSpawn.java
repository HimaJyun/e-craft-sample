package io.e_craft.movetospawn;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class MoveToSpawn extends JavaPlugin {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 実装にはいくつかパターンがあります。
        // 例示メソッドをいくつか用意しているので用途に合わせて好きな方法を選びましょう。
        argsWorld(sender, args);
        return true;
    }

    /**
     * プレイヤーが今いる世界のスポーン地点に移動させる。
     *
     * @param player 移動させたいプレイヤー
     */
    private void currentWorld(Player player) {
        // Playerが今いる世界を取得
        World world = player.getWorld();
        // 世界のスポーン地点を取得
        Location location = world.getSpawnLocation();
        // 移動させる
        player.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
        // もちろん1行で書いても構いません
        //player.teleport(player.getWorld().getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    /**
     * 設定した世界のスポーン地点に移動させる
     *
     * @param player 移動させたいプレイヤー
     */
    private void configWorld(Player player) {
        // 今回は"world"で決め打ちにしてますが、実際には世界名を設定などからロードします。
        String config = "world";
        // 例えばこんな感じ
        //String config = getConfig().getString("spawnTo");

        // ここから先は指定した世界に移動させるパターンとほぼ同じ
        // 世界を取得する
        World world = Bukkit.getWorld(config);
        // worldがnull = 指定された世界が存在しない。
        if (world == null) {
            // 設定した世界が存在しない=そもそも設定が間違っているという事なのでエラーはコンソールに表示させる方が良いでしょう。
            player.sendMessage("Configuration error!!");
            getLogger().severe("World not found");
            return;
        }

        // 世界のスポーン地点を取得
        Location location = world.getSpawnLocation();
        // 移動させる
        player.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    /**
     * 引数で指定された世界に移動させる。
     * 引数がない時は今いる世界のスポーン地点に移動させる。
     *
     * @param sender コマンドを実行したプレイヤー
     * @param args   引数
     */
    private void argsWorld(CommandSender sender, String[] args) {
        // プレイヤーにしか使えないので、onCommandなどで使う場合はコンソールかチェックしましょう
        if (!(sender instanceof Player)) {
            sender.sendMessage("Player only");
            return;
        }
        // CommandSenderにもteleport()はありますが、getWorld()などはエンティティ専用です。
        Player player = (Player)sender;

        // 今回は省きますが、権限チェックなども必要になるでしょう。
        /*if(!player.hasPermission("permission.spawn")) {
            player.sendMessage("You don't have permission!");
            return;
        }*/

        // 引数がある場合は世界を取得、ない場合は今いる世界のスポーン地点を利用
        World world;
        if(args.length == 0) {
            // 引数がない場合
            world = player.getWorld();
        } else {
            // 引数がある場合
            world = Bukkit.getWorld(args[0]);
            // 指定された世界が存在しない場合も考慮しましょう。
            if(world == null) {
                player.sendMessage("World not found");
                return;
            }
        }

        // 世界のスポーン地点を取得
        Location location = world.getSpawnLocation();
        // 移動させる
        player.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

}
