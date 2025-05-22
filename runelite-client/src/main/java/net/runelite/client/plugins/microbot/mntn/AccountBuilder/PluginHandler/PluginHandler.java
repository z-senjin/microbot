package net.runelite.client.plugins.microbot.mntn.AccountBuilder.PluginHandler;

import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.microbot.Microbot;

import java.util.Collection;

public class PluginHandler {

    private Plugin currentPlugin = null;

    private Collection<Plugin> pluginList = null;

    public PluginHandler(){
        // get list of plugins and set them
        pluginList = Microbot.getPluginManager().getPlugins();
    }

    public void startPlugin(Plugin plugin){
        if(!isPluginEnabled(plugin)){
            Microbot.log("Starting plugin: " + plugin.getName());
            Microbot.startPlugin(plugin);
        }
    }

    public void stopPlugin(Plugin plugin){
        if(isPluginEnabled(plugin)){
            Microbot.log("Stopping plugin: " + plugin.getName());
            Microbot.stopPlugin(plugin);
        }
    }

    public Plugin findPluginByName(String name) {
        return pluginList.stream()
                .filter(p -> p.getName().contains(name))
                .findFirst()
                .orElse(null);
    }

    public boolean isPluginEnabled(Plugin plugin){
       return Microbot.getPluginManager().isPluginEnabled(plugin);
    }

    public Plugin getCurrentPlugin(){
        return currentPlugin;
    }

    public void setCurrentPlugin(Plugin plugin){
        System.out.println("Setting Current Plugin!");
        this.currentPlugin = plugin;
    }

    public void resetCurrentPlugin(){
        this.currentPlugin = null;
    }
}
