package de.banarnia.simplewarps.gui;

import de.banarnia.simplewarps.api.UtilGUI;
import de.banarnia.simplewarps.lang.Message;
import de.banarnia.simplewarps.manager.Warp;
import de.banarnia.simplewarps.manager.WarpManager;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.util.Legacy;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;

public class WarpGUI {

    private WarpManager manager;
    private Player player;
    private final PaginatedGui gui;

    private boolean isAdmin;

    public WarpGUI(WarpManager manager, Player player) {
        this.manager = manager;
        this.player = player;

        this.gui = Gui.paginated().title(Legacy.SERIALIZER.deserialize("§eWarps")).rows(2).pageSize(9).create();
        this.gui.setDefaultClickAction(event -> event.setCancelled(true));
        this.gui.setOpenGuiAction(event -> init());

        isAdmin = player.hasPermission("simplewarps.admin");
    }

    private void init() {
        gui.clearPageItems();

        gui.getFiller().fillBottom(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).setName(" ").asGuiItem());

        Collection<Warp> warps = isAdmin ? manager.getWarps() : manager.getUsableWarps(player);
        for (Warp warp : warps)
            gui.addItem(getWarpItem(warp));

        UtilGUI.setPaginationItems(gui, Message.GUI_WARP_PAGE_PREVIOUS.get(), Message.GUI_WARP_PAGE_NEXT.get());
        gui.update();
    }

    // ~~~~~ Items ~~~~~

    private GuiItem getWarpItem(Warp warp) {
        GuiItem guiItem = new GuiItem(buildWarpIcon(warp));
        guiItem.setAction(click -> {
            // Check if player should be warped.
            if (!isAdmin || (click.isLeftClick() && !click.isShiftClick())) {
                manager.executeWarp(player, player, warp);
                return;
            }

            // Shift-Leftclick to edit the warp.
            if (click.isLeftClick() && click.isShiftClick()) {
                new WarpEditGUI(manager, warp, player).open(player);
                return;
            }

            // Rightclick to delete warp.
            new ConfirmationGUI(Message.GUI_CONFIRMATION_TITLE.get(), delete -> {
                if (delete)
                    manager.executeDelwarp(click.getWhoClicked(), warp);

                gui.open(player);
            }).open(player);
        });

        return guiItem;
    }

    public ItemStack buildWarpIcon(Warp warp) {
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

        Timestamp ts = new Timestamp(warp.getCreated());
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(ts.getTime());
        String year     = String.valueOf(cal.get(Calendar.YEAR));
        String month    = String.valueOf(cal.get(Calendar.MONTH));
        String day      = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
        String hour     = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
        String minute   = String.valueOf(cal.get(Calendar.MINUTE));
        String second   = String.valueOf(cal.get(Calendar.SECOND));

        String lore = isAdmin ? Message.GUI_WARP_LORE_ADMIN.get() : Message.GUI_WARP_LORE.get();
        lore = lore.replace("%enabled%", warp.isEnabled() ? "§2✔" : "§4✖");
        lore = lore.replace("%permission%", warp.requiresPermission() ? "§esimplewarps.warp." + warp.getName() : "§4✖");
        lore = lore.replace("%warp_worldname%", warp.getWorldName());
        lore = lore.replace("%year%", year);
        lore = lore.replace("%month%", month);
        lore = lore.replace("%day%", day);
        lore = lore.replace("%hour%", hour);
        lore = lore.replace("%minute%", minute);
        lore = lore.replace("%second%", second);

        ItemBuilder builder = ItemBuilder.from(material);
        builder.setName(Message.GUI_WARP_NAME.replace("%warp_name%", warp.getName()));
        builder.setLore(lore.split("\n"));

        return builder.build();
    }

    public boolean open(Player player) {
        gui.open(player);
        return true;
    }

}
