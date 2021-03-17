package io.e_craft.votifierusage;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class VotifierUsage extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll((Plugin) this);
    }

    @EventHandler
    public void onVotifierEvent(VotifierEvent e) {
        Vote v = e.getVote();
        Bukkit.broadcastMessage("getAddress(): " + v.getAddress());
        Bukkit.broadcastMessage("getServiceName(): " + v.getServiceName());
        Bukkit.broadcastMessage("getTimeStamp(): " + v.getTimeStamp());
        Bukkit.broadcastMessage("getUsername(): " + v.getUsername());
        Bukkit.broadcastMessage("getLocalTimestamp(): " + v.getLocalTimestamp());
    }
}
