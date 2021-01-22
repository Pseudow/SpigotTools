package net.pseudow.tools.command;

import net.pseudow.tools.reflection.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CommandManager {
    private String permissionMissingMessage, argumentsTooLong;

    public CommandManager() {
        this.permissionMissingMessage = ChatColor.RED + "You don't have the permission to execute this command!";
    }

    public CommandManager setArgumentsTooLong(String message) {
        this.argumentsTooLong = message;
        return this;
    }

    public CommandManager setPermissionMissingMessage(String message) {
        this.permissionMissingMessage = message;
        return this;
    }

    public CommandManager registerCommand(Object command) {
        for (Method method : command.getClass().getMethods()) {
            if (method.isAnnotationPresent(CommandHandler.class)) {
                final CommandHandler annotation = method.getAnnotation(CommandHandler.class);
                final CommandTemplate commandTemplate = new CommandTemplate(annotation.name(), annotation.description(), annotation.usage(),
                        Arrays.asList(annotation.aliases()), (commandSender, label, arguments) -> {

                    if (annotation.requirePlayer() && !(commandSender instanceof Player))
                        return true;

                    if (arguments.length != annotation.argsLength()) {
                        commandSender.sendMessage(this.argumentsTooLong == null || this.argumentsTooLong.equals("") ? annotation.usage() :
                                this.argumentsTooLong.replace("%command%", label).replace("%args_length%", Integer.toString(arguments.length)).toString());
                        return true;
                    }

                    for (String permission : annotation.permissions())
                        if (!commandSender.hasPermission(permission))
                            commandSender.sendMessage(permissionMissingMessage);

                    if (method.getReturnType() == boolean.class) {
                        return (boolean) Reflection.invokeMethod(command, method.getName(), commandSender, label, arguments);
                    }

                    return false;
                });

                ((SimpleCommandMap) Objects.requireNonNull(Reflection.invokeMethod(Bukkit.getServer(), "getCommandMap"))).register(annotation.name(), commandTemplate);
            }
        }
        return this;
    }

    public static class CommandTemplate extends BukkitCommand {
        private final Action action;

        protected CommandTemplate(String name, String description, String usageMessage, List<String> aliases, Action action) {
            super(name, description, usageMessage, aliases);
            this.action = action;
        }

        @Override
        public boolean execute(CommandSender commandSender, String s, String[] strings) {
            return action.execute(commandSender, s, strings);
        }
    }

    private interface Action {
        boolean execute(CommandSender commandSender, String label, String[] arguments);
    }
}
