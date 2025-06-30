package de.banarnia.simplewarps.gui;

import de.banarnia.simplewarps.api.UtilGUI;
import de.banarnia.simplewarps.lang.Message;
import de.banarnia.simplewarps.manager.Warp;
import de.banarnia.simplewarps.manager.WarpManager;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.GuiType;
import dev.triumphteam.gui.components.util.Legacy;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public class WarpEditGUI {

    private final Gui gui;

    private WarpManager manager;
    private Warp warp;

    public WarpEditGUI(WarpManager manager, Warp warp, Player player) {
        this.manager = manager;
        this.warp = warp;

        this.gui = Gui.gui().title(Legacy.SERIALIZER.deserialize("§fWarp: §e" + warp.getName())).rows(1).create();
        this.gui.setDefaultClickAction(event -> event.setCancelled(true));
        this.gui.setOpenGuiAction(event -> init());
        //this.gui.setCloseGuiAction(c -> new WarpGUI(manager, player).open(player));
    }

    private void init() {
        gui.getFiller().fill(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).setName(" ").asGuiItem());

        gui.setItem(1, 3, getIconItem());
        gui.setItem(1, 5, getEnableItem());
        gui.setItem(1, 7, getRequirePermissionItem());

        gui.update();
    }

    private GuiItem getIconItem() {
        String icon = warp.getIcon();
        World.Environment environment = warp.getWorldEnvironment();
        Material material = icon != null ? Material.getMaterial(icon) : null;
        if (material == null) {
            switch (environment) {
                case NETHER:
                    material = Material.NETHERRACK;
                    break;
                case THE_END:
                    material = Material.END_STONE;
                    break;
                default:
                    material = Material.GRASS_BLOCK;
            }
        }

        ItemBuilder builder = ItemBuilder.from(material);
        builder.setName(Message.GUI_WARP_SETTINGS_ICON.get());

        GuiItem item = new GuiItem(builder.build());
        item.setAction(click -> {
            new MaterialSelectionGUI(Message.GUI_ICON_SELECTION_TITLE.get(), item.getItemStack().getType(),
                                     Message.GUI_SAVE_NAME.get(), Message.GUI_CANCEL_NAME.get(), selection -> {
                if (selection != null) {
                    warp.setIcon(selection.toString());
                    manager.updateWarp(warp);
                }

                gui.open(click.getWhoClicked());
            }).open((Player) click.getWhoClicked());
        });

        return item;
    }

    private GuiItem getEnableItem() {
        ItemBuilder builder = ItemBuilder.from(warp.isEnabled() ? Material.LIME_DYE : Material.GRAY_DYE);
        builder.setName(Message.GUI_WARP_SETTINGS_ENABLE.get());

        GuiItem item = new GuiItem(builder.build());
        item.setAction(click -> {
            warp.setEnabled(!warp.isEnabled());
            manager.updateWarp(warp);
            gui.open(click.getWhoClicked());
        });

        return item;
    }

    private GuiItem getRequirePermissionItem() {
        ItemBuilder builder = ItemBuilder.from(warp.requiresPermission() ? Material.LIME_DYE : Material.GRAY_DYE);
        builder.setName(Message.GUI_WARP_SETTINGS_PERMISSION.replace("%permission%", "simplewarps.warp." + warp.getName().toLowerCase()));

        GuiItem item = new GuiItem(builder.build());
        item.setAction(click -> {
            warp.setRequiresPermission(!warp.requiresPermission());
            manager.updateWarp(warp);
            gui.open(click.getWhoClicked());
        });

        return item;
    }

    public boolean open(Player player) {
        gui.open(player);
        return true;
    }

}
