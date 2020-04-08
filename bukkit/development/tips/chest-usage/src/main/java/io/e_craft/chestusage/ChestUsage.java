package io.e_craft.chestusage;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ChestUsage extends JavaPlugin implements Listener, Runnable {

    // region 置いたものが全てチェストに変わる
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void changeChest(BlockPlaceEvent e) {
        // ブロックを取得
        Block block = e.getBlock();

        // チェストなら何もしない(そうしないとラージチェストが作れなくなる)
        if (block.getType() == Material.CHEST) {
            return;
        }

        // チェストに変えてしまう
        block.setType(Material.CHEST);
    }
    // endregion

    // region 触れたチェストの中身が所持品と同じになる
    @EventHandler(ignoreCancelled = true)
    public void copyInventory(PlayerInteractEvent e) {
        // ブロックを右クリックしたとき以外は何もしない
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // クリックしたブロックがチェストじゃないなら何もしない
        Block block = e.getClickedBlock();
        if (block.getType() != Material.CHEST) {
            return;
        }

        // 開けようとしたプレイヤー
        Player player = e.getPlayer();
        // プレイヤーのInventoryを取得
        Inventory playerInventory = player.getInventory();

        // キャストする
        org.bukkit.block.Chest chest = (org.bukkit.block.Chest) block.getState();
        // Inventoryを取得
        Inventory inventory = chest.getBlockInventory();

        // 中身を複製する(空にしてコピーする)
        inventory.clear();
        for (ItemStack item : playerInventory) {
            // 何もないところはnullになるので注意
            if (item != null) {
                inventory.addItem(item.clone());
            }
        }
    }
    // endregion

    // region 正面からしか開けられないチェスト
    // 正面の方向
    private final static Map<BlockFace, BlockFace> FRONT = new EnumMap<>(BlockFace.class);

    static {
        // 正面を定義
        FRONT.put(BlockFace.NORTH, BlockFace.SOUTH);
        FRONT.put(BlockFace.EAST, BlockFace.WEST);
        FRONT.put(BlockFace.SOUTH, BlockFace.NORTH);
        FRONT.put(BlockFace.WEST, BlockFace.EAST);
    }

    @EventHandler(ignoreCancelled = true)
    public void frontOnly(PlayerInteractEvent e) {
        // ブロックを右クリックしたとき以外は何もしない
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // クリックしたブロックがチェストじゃないなら何もしない
        Block block = e.getClickedBlock();
        if (block.getType() != Material.CHEST) {
            return;
        }

        // 開けようとしたプレイヤー
        Player player = e.getPlayer();
        // プレイヤーの向き
        BlockFace playerFace = player.getFacing();

        // キャストする
        org.bukkit.block.data.type.Chest chest = (org.bukkit.block.data.type.Chest) block.getBlockData();
        // 今の向き
        BlockFace current = chest.getFacing();
        // 正面を取得
        BlockFace face = FRONT.get(current);

        // 正面以外なら開かないようにする
        if (playerFace != face) {
            e.setCancelled(true);
        }

        // ちなみに、後ろからしか開けられないチェストはもっと簡単
        // 同じ方向を向いているか確認するだけ
        /*
        if (playerFace != current) {
            e.setCancelled(true);
        }
        //*/
    }
    // endregion

    // region 回るチェスト
    // 対象になるチェストの位置
    // (複数チックの間で同じブロックを保持し続けるとおかしくなる可能性があるので、Locationで取得して都度getBlock()する方が良いかも知れません)
    private final List<Location> rotationChestLocation = new LinkedList<>();
    // 回転方向テーブル
    private final static Map<BlockFace, BlockFace> ROTATION_DIRECTION = new HashMap<>();

    static {
        // KeyとValueを入れ替えると逆回転になる
        ROTATION_DIRECTION.put(BlockFace.NORTH, BlockFace.EAST);
        ROTATION_DIRECTION.put(BlockFace.EAST, BlockFace.SOUTH);
        ROTATION_DIRECTION.put(BlockFace.SOUTH, BlockFace.WEST);
        ROTATION_DIRECTION.put(BlockFace.WEST, BlockFace.NORTH);
    }

    @EventHandler(ignoreCancelled = true)
    public void rotationChest(BlockPlaceEvent e) {
        // ブロックの取得
        Block block = e.getBlock();

        // チェスト以外なら何もしない
        if (block.getType() != Material.CHEST) {
            return;
        }

        // ブロックの位置を記憶しておく
        rotationChestLocation.add(block.getLocation().clone());
    }

    @Override
    public void run() {
        // リストのイテレーション中に要素を削除するにはIteratorが必要です。 (forは使えません)
        Iterator<Location> iterator = rotationChestLocation.iterator();
        while (iterator.hasNext()) {
            // Locationを取得
            Location location = iterator.next();
            // ブロックを取得
            Block block = location.getBlock();

            // チェストじゃない(チェストじゃなくなった)ならリストから削除
            if (block.getType() != Material.CHEST) {
                iterator.remove();
                return;
            }

            // キャスト
            org.bukkit.block.data.type.Chest chest = (org.bukkit.block.data.type.Chest) block.getBlockData();

            // ラージチェストも対象外にしておきます
            if (chest.getType() != org.bukkit.block.data.type.Chest.Type.SINGLE) {
                iterator.remove();
                return;
            }

            // 現在の方向を取得
            BlockFace current = chest.getFacing();
            // 新しい方向を取得
            BlockFace newFacing = ROTATION_DIRECTION.get(current);
            // 方向変換
            chest.setFacing(newFacing);

            // 新しい方向を設定
            block.setBlockData(chest);
        }
    }
    // endregion

    // region チェストの種類を確認
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void chestType(PlayerInteractEvent e) {
        // ブロックを右クリックしたとき以外は何もしない
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // ブロックを取得
        Block block = e.getClickedBlock();

        // チェストじゃないなら何もしない
        if (block.getType() != Material.CHEST) {
            return;
        }

        // キャストする
        org.bukkit.block.data.type.Chest chest = (org.bukkit.block.data.type.Chest) block.getBlockData();

        // プレイヤーを取得
        Player player = e.getPlayer();
        // 種類に応じてメッセージを出す
        if (chest.getType() == org.bukkit.block.data.type.Chest.Type.SINGLE) {
            player.sendMessage("Single Chest");
        } else {
            player.sendMessage("Large Chest");
        }
    }
    // endregion

    // region ラージチェストの反対側を取得
    // 取得した方向と反対側の方向の組み合わせをルックアップテーブルにする
    private final static Map<BlockFace, BlockFace> LEFT_TABLE = new EnumMap<>(BlockFace.class);
    private final static Map<BlockFace, BlockFace> RIGHT_TABLE = new EnumMap<>(BlockFace.class);

    static {
        // 自分の方向と反対側の方向のペアを用意
        LEFT_TABLE.put(BlockFace.NORTH, BlockFace.EAST);
        LEFT_TABLE.put(BlockFace.SOUTH, BlockFace.WEST);
        LEFT_TABLE.put(BlockFace.WEST, BlockFace.NORTH);
        LEFT_TABLE.put(BlockFace.EAST, BlockFace.SOUTH);

        RIGHT_TABLE.put(BlockFace.NORTH, BlockFace.WEST);
        RIGHT_TABLE.put(BlockFace.SOUTH, BlockFace.EAST);
        RIGHT_TABLE.put(BlockFace.WEST, BlockFace.SOUTH);
        RIGHT_TABLE.put(BlockFace.EAST, BlockFace.NORTH);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void largeChestPair(PlayerInteractEvent e) {
        // ブロックを右クリックしたとき以外は何もしない
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // ブロックを取得
        Block block = e.getClickedBlock();

        // チェストじゃないなら何もしない
        if (block.getType() != Material.CHEST) {
            return;
        }

        // キャストする
        org.bukkit.block.data.type.Chest chest = (org.bukkit.block.data.type.Chest) block.getBlockData();

        // 通常のチェストなら何もしない
        if (chest.getType() == org.bukkit.block.data.type.Chest.Type.SINGLE) {
            return;
        }

        // 自分側のチェストがどちらかに応じて使うテーブルを変える
        Map<BlockFace, BlockFace> table = null;
        if (chest.getType() == org.bukkit.block.data.type.Chest.Type.LEFT) {
            table = LEFT_TABLE;
        } else if (chest.getType() == org.bukkit.block.data.type.Chest.Type.RIGHT) {
            table = RIGHT_TABLE;
        }

        // 方向を取得
        BlockFace face = chest.getFacing();

        // ルックアップテーブルを使って反対側の方向を特定
        BlockFace pairFace = table.get(face);

        // 特定した方向に反対側のチェストがあります。
        Block pair = block.getRelative(pairFace);

        // メッセージを送信
        Player player = e.getPlayer();
        player.sendMessage(pair.toString());
    }
    // endregion

    private BukkitTask task;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        task = getServer().getScheduler().runTaskTimer(this, this, 0, 2); // あんまり早いと回ってる感がない
    }

    @Override
    public void onDisable() {
        //task.cancel();
        HandlerList.unregisterAll((Plugin) this);
    }
}
