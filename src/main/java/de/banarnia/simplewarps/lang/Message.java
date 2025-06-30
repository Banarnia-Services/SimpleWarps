package de.banarnia.simplewarps.lang;

import de.banarnia.api.lang.ILanguage;

public enum Message implements ILanguage {
    PREFIX("§8[§6SimpleWarps§8]§7"),
    COMMAND_ERROR_CONSOLE_NOT_SUPPORTED("%prefix% §cThis command may not be executed by console."),

    // Command /warp
    COMMAND_INFO_WARP_CONFIG_RELOADED("%prefix% The config has been reloaded."),
    COMMAND_INFO_WARP_TELEPORT("%prefix% Warping to §e%warp%§7..."),
    COMMAND_INFO_WARP_OTHERS("%prefix% Warping §e%player% §7to §e%warp%§7."),
    COMMAND_INFO_WARP_WARMUP("%prefix% You will be warped to §e%warp% §7in §e%time%s§7."),
    COMMAND_ERROR_PERMISSION("%prefix% §cYou do not have the permission for that."),
    COMMAND_ERROR_WARP_WARMUP_ABORT("%prefix% §cWarp aborted..."),
    COMMAND_ERROR_WARP_COOLDOWN("%prefix% §cYou can't warp again within the next §e%time%s§c."),
    COMMAND_ERROR_WARP_NOT_FOUND("%prefix% §cCould not find a warp with the name §e%warp%§c."),
    COMMAND_ERROR_WARP_DISABLED("%prefix% §cThis warp is disabled."),
    COMMAND_ERROR_WARP_LOCATION_NOT_LOADED("%prefix% §cYou can't warp there because the world is not loaded."),
    COMMAND_ERROR_WARP_FAILED("%prefix% §cCouldn't warp to the destination."),

    // Command /setwarp
    COMMAND_INFO_SETWARP_UPDATED("%prefix% The location of warp §e%warp% §7has been updated."),
    COMMAND_INFO_SETWARP_CREATED("%prefix% The warp §e%warp% §7has been created."),

    // Command /delwarp
    COMMAND_INFO_DELWARP_FINISHED("%prefix% The warp §e%warp% §7has been deleted."),

    // GUI
    GUI_ICON_SELECTION_TITLE("§7Select a new icon"),
    GUI_SAVE_NAME("§aSave"),
    GUI_CANCEL_NAME("§cCancel"),
    GUI_CONFIRMATION_TITLE("§7Delete warp?"),
    GUI_WARP_NAME("§a%warp_name%"),
    GUI_WARP_LORE(
            "§7World: §e%warp_worldname%\n" +
            "\n" +
            "§eClick to warp"),
    GUI_WARP_LORE_ADMIN(
            "§7World: §e%warp_worldname%\n" +
                    "§7%year%-%month%-%day% %hour%:%minute%:%second%\n" +
                    "\n" +
                    "§7Enabled: %enabled%\n" +
                    "§7Permission required: %permission%\n" +
                    "\n" +
                    "§eLeftclick to warp\n" +
                    "§eShift-Leftclick to edit the warp\n" +
                    "§cRightclick to delete"),
    GUI_WARP_SETTINGS_ICON("§aChange warp icon"),
    GUI_WARP_SETTINGS_ENABLE("§aEnable or disable warp"),
    GUI_WARP_SETTINGS_PERMISSION("§aRequire permission: §e%permission%"),
    GUI_WARP_PAGE_PREVIOUS("§cBack"),
    GUI_WARP_PAGE_NEXT("§aNext")
    ;

    String defaultMessage, message;

    Message(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    @Override
    public String getKey() {
        return this.toString().toLowerCase().replace("_", "-");
    }

    @Override
    public String getDefaultMessage() {
        return defaultMessage;
    }

    @Override
    public String get() {
        String message = this.message != null ? this.message : defaultMessage;
        if (this == PREFIX)
            return message;

        return message.replace("%prefix%", PREFIX.get());
    }

    @Override
    public void set(String message) {
        this.message = message;
    }
}
