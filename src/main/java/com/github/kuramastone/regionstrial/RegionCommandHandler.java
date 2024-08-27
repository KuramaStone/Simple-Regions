package com.github.kuramastone.regionstrial;

import com.github.kuramastone.regionstrial.gui.FlagGUI;
import com.github.kuramastone.regionstrial.gui.RegionManagerGUI;
import com.github.kuramastone.regionstrial.regions.FlagScope;
import com.github.kuramastone.regionstrial.regions.Region;
import com.github.kuramastone.regionstrial.regions.RegionFlag;
import com.github.kuramastone.regionstrial.gui.RegionSelectionGUI;
import com.github.kuramastone.regionstrial.selection.PlayerSelection;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class RegionCommandHandler implements CommandExecutor, TabExecutor {

    private RegionAPI api;

    public RegionCommandHandler(RegionAPI api) {
        this.api = api;
    }

    public void register(JavaPlugin plugin) {
        plugin.getCommand("region").setTabCompleter(this);
        plugin.getCommand("region").setExecutor(this);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>(); // No tab completions for console
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission(RegionAPI.createRegionPermission)) {
                completions.addAll(Arrays.asList("create", "wand", "section", "rename", "whitelist", "flag", "manage"));
            }
        }
        else if (args.length == 2) {
            String subcommand = args[0].toLowerCase();

            switch (subcommand) {
                case "section":
                    completions.addAll(Arrays.asList("add", "remove", "relocate", "view"));
                    break;
                case "create":
                case "add":
                case "remove":
                case "rename":
                case "flag":
                    completions = predictSuggestionStartingWith(args[1], getRegionNames());
                    break;
                case "whitelist":
                    completions = predictSuggestionStartingWith(args[1], getRegionNames());
                    break;
                default:
                    break;
            }
        }
        else if (args.length == 3) {

            String subcommand = args[0].toLowerCase();

            switch (subcommand) {
                case "flag":
                    completions = predictSuggestionStartingWith(args[2], api.getRegionFlagNames());
                    break;
                case "whitelist":
                    completions = predictSuggestionStartingWith(args[2], getPlayersAndTargetSuggestion(sender));
                    break;
                case "create":
                case "add":
                case "remove":
                    Region region = api.getRegion(args[1]);
                    if (region != null) {
                        completions = predictSuggestionStartingWith(args[2], region.getSectionNames());
                    }
                    break;
                case "rename":
                    completions = predictSuggestionStartingWith(args[2], getRegionNames());
                    break;
                case "section":

                    // switch case for selection subcommands
                    switch (args[1].toLowerCase()) {
                        case "add":
                        case "remove":
                        case "view":
                        case "relocate":
                            completions = predictSuggestionStartingWith(args[2], getRegionNames());
                            break;
                        default:
                            break;
                    }


                default:
                    break;
            }
        }
        else if (args.length == 4) {
            String subcommand = args[0].toLowerCase();
            if (subcommand.equalsIgnoreCase("section")) {
                switch (args[1].toLowerCase()) {
                    case "add":
                    case "remove":
                    case "view":
                    case "relocate":
                        Region region = api.getRegion(args[2]);
                        if (region != null) {
                            completions = predictSuggestionStartingWith(args[3], region.getSectionNames());
                        }
                        break;
                    default:
                        break;
                }
            }
            else if (args[0].equalsIgnoreCase("flag")) {
                completions = predictSuggestionStartingWith(args[3], api.getFlagScopeNames());
            }
        }

        return completions;
    }

    private Collection<String> getRegionNames() {
        return api.getRegionNames();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length < 1) {
            if (sender instanceof Player player) {
                if (!sender.hasPermission(RegionAPI.menuPermission)) {
                    sender.sendMessage(api.getMessage("commands.insufficient_permissions"));
                    return true;
                }


                RegionSelectionGUI.openRegionMenu(player);
                return true;
            }

            sender.sendMessage("Usage: /region <subcommand>");
            return true;
        }

        String subcommand = args[0];

        switch (subcommand.toLowerCase()) {
            case "create":
                handleCreateCommand(sender, args);
                break;
            case "wand":
                handleWandCommand(sender);
                break;
            case "whitelist":
                handleWhitelistCommand(sender, args);
                break;
            case "section":
                handleSectionCommand(sender, args);
                break;
            case "rename":
                handleRenameCommand(sender, args);
                break;
            case "flag":
                handleFlagCommand(sender, args);
                break;
            case "manage":
                handleManageCommand(sender, args);
                break;
            default:
                handleDefaultCommand(sender, args);
                break;
        }
        return true;
    }

    private void handleManageCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission(RegionAPI.createRegionPermission)) {
            sender.sendMessage(api.getMessage("commands.insufficient_permissions"));
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can manage regions via gui.");
            return;
        }
        Player player = (Player) sender;

        if (args.length <= 1) {
            RegionSelectionGUI.openRegionMenu(player);
            return;
        }
        else {
            String regionName = args[1];

            Region region = api.getRegion(regionName);
            if (region == null) {
                sender.sendMessage(api.getMessage("commands.regions.unknown_region").replace("{region}", regionName));
                return;
            }

            RegionManagerGUI.openManagerMenu(player, region);
        }
    }

    /*
    Provide tools to view/add/remove a section
     */
    private void handleSectionCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission(RegionAPI.createRegionPermission)) {
            sender.sendMessage(api.getMessage("commands.insufficient_permissions"));
            return;
        }

        if (args.length < 2) {
            sendUsageMessage(sender);
            return;
        }

        switch (args[1].toLowerCase()) {
            case "add":
                handleAddSection(sender, args);
                break;
            case "remove":
                handleRemoveSection(sender, args);
                break;
            case "relocate":
                handleRelocateSection(sender, args);
                break;
            case "view":
                handleViewSection(sender, args);
                break;
            default:
                sendUsageMessage(sender);
                break;
        }
    }

    private void sendUsageMessage(CommandSender sender) {
        sender.sendMessage("Usage: /region section <subcommand> [parameters]");
    }

    private void handleAddSection(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("Usage: /region section add <region> <section>");
            return;
        }

        String regionName = args[2];
        String additionName = args[3];

        Region region = api.getRegion(regionName);
        if (region == null) {
            sender.sendMessage(api.getMessage("commands.regions.unknown_region").replace("{region}", regionName));
            return;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use the selection tool.");
            return;
        }

        if (region.doesSectionExist(additionName)) {
            sender.sendMessage(api.getMessage("commands.regions.section_already_exists")
                    .replace("{region}", regionName)
                    .replace("{section}", additionName));
            return;
        }

        PlayerSelection selection = api.getOrCreateSelectionOf(player);
        if (!selection.isReady()) {
            sender.sendMessage(api.getMessage("commands.regions.selection_not_ready")
                    .replace("{region}", regionName)
                    .replace("{section}", additionName));
            return;
        }

        region.addSection(selection.selectionToRegion(additionName));
        player.sendMessage(api.getMessage("commands.regions.section.add_success")
                .replace("{region}", region.getName())
                .replace("{section}", additionName));
    }

    private void handleRemoveSection(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("Usage: /region section remove <region> <section>");
            return;
        }

        String regionName = args[2];
        String additionName = args[3];

        Region region = api.getRegion(regionName);
        if (region == null) {
            sender.sendMessage(api.getMessage("commands.regions.unknown_region").replace("{region}", regionName));
            return;
        }

        if (!region.doesSectionExist(additionName)) {
            sender.sendMessage(api.getMessage("commands.regions.section_doesnt_exist")
                    .replace("{region}", regionName)
                    .replace("{section}", additionName));
            return;
        }

        region.removeSection(additionName);
        sender.sendMessage(api.getMessage("commands.regions.section.remove_success")
                .replace("{region}", region.getName())
                .replace("{section}", additionName));
    }

    private void handleRelocateSection(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("Usage: /region section relocate <region> <section>");
            return;
        }

        String regionName = args[2];
        String sectionName = args[3];

        Region region = api.getRegion(regionName);
        if (region == null) {
            sender.sendMessage(api.getMessage("commands.regions.unknown_region").replace("{region}", regionName));
            return;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use the selection tool.");
            return;
        }

        if (!region.doesSectionExist(sectionName)) {
            sender.sendMessage(api.getMessage("commands.regions.section_doesnt_exist")
                    .replace("{region}", regionName)
                    .replace("{section}", sectionName));
            return;
        }

        PlayerSelection selection = api.getOrCreateSelectionOf(player);
        if (!selection.isReady()) {
            sender.sendMessage(api.getMessage("commands.regions.selection_not_ready")
                    .replace("{region}", regionName)
                    .replace("{section}", sectionName));
            return;
        }

        region.relocate(sectionName, selection);
        player.sendMessage(api.getMessage("commands.regions.section.relocate_success")
                .replace("{region}", region.getName())
                .replace("{section}", sectionName));
    }

    private void handleViewSection(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("Usage: /region section view <region> <section>");
            return;
        }

        String regionName = args[2];
        String sectionName = args[3];

        Region region = api.getRegion(regionName);
        if (region == null) {
            sender.sendMessage(api.getMessage("commands.regions.unknown_region").replace("{region}", regionName));
            return;
        }

        if (region.isOutlineActive(sectionName)) {
            region.stopUpdateOutlineEffect(sectionName);
            sender.sendMessage(api.getMessage("commands.regions.section.view_success_off")
                    .replace("{region}", region.getName())
                    .replace("{section}", sectionName));
        }
        else {
            region.startUpdateOutlineEffect(sectionName);
            sender.sendMessage(api.getMessage("commands.regions.section.view_success_on")
                    .replace("{region}", region.getName())
                    .replace("{section}", sectionName));
        }
    }


    private void handleWandCommand(CommandSender sender) {
        if (!sender.hasPermission(RegionAPI.selectionPermission)) {
            sender.sendMessage(api.getMessage("commands.insufficient_permissions"));
            return;
        }

        if (sender instanceof Player player) {
            player.getInventory().addItem(api.getWandItem());
            api.getOrCreateSelectionOf(player);
            player.sendMessage(api.getMessage("commands.regions.wand.success"));
        }
        else {
            sender.sendMessage("Only players may use a selection wand.");
            return;
        }

    }

    private void handleRenameCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission(RegionAPI.createRegionPermission)) {
            sender.sendMessage(api.getMessage("commands.insufficient_permissions"));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage("Usage: /region rename <region> <new name>");
            return;
        }

        String regionName = args[1];
        String newName = args[2];

        Region region = api.getRegion(regionName);
        if (api.getRegion(newName) != null) {
            sender.sendMessage(api.getMessage("commands.regions.region_already_exists").replace("{region}", regionName));
            return;
        }
        if (region == null) {
            sender.sendMessage(api.getMessage("commands.regions.unknown_region").replace("{region}", regionName));
            return;
        }

        sender.sendMessage(api.getMessage("commands.regions.rename.success").replace("{region}", region.getName()));
        api.renameRegion(newName, region);
    }

    private void handleCreateCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission(RegionAPI.createRegionPermission)) {
            sender.sendMessage(api.getMessage("commands.insufficient_permissions"));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage("Usage: /region create <region> ");
            return;
        }

        String regionName = args[1];
        Region region = api.getRegion(regionName);

        if (region != null) {
            sender.sendMessage(api.getMessage("commands.regions.region_already_exists").replace("{region}", regionName));
            return;
        }
        region = new Region(regionName, new LinkedHashMap<>(), new HashSet<>(), new HashMap<>());

        api.addRegion(region);
        sender.sendMessage(api.getMessage("commands.regions.create.success").replace("{region}", region.getName()));

    }

    /*
    List current users

    Example message format:

    commands.regions.list_parent = """
                        All whitelisted players:
                        {list}"""
    commands.regions.list_child = "  - {id}\n"

     */
    private void handleWhitelistCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission(RegionAPI.modifyWhitelistPermission)) {
            sender.sendMessage(api.getMessage("commands.insufficient_permissions"));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage("Usage: /region whitelist <region> <player>");
            return;
        }
        else if (args.length == 2) {


            String regionName = args[1];
            Region region = api.getRegion(regionName);

            if (region == null) {
                sender.sendMessage(api.getMessage("commands.regions.unknown_region").replace("{region}", regionName));
                return;
            }
            StringBuilder listText = new StringBuilder();

            for (UUID uuid : region.getWhitelistedEntities()) {
                String id = uuid.toString();

                // if uuid is not a currently alive entity, use player name
                if (Bukkit.getServer().getEntity(uuid) == null) {
                    OfflinePlayer plyr = Bukkit.getOfflinePlayer(uuid);
                    id = plyr.getName();
                }

                if (id == null) {
                    id = uuid.toString();
                }
                listText.append(api.getMessage("commands.regions.whitelist.list_child").replace("{id}", id));
            }

            String fullMessage = api.getMessage("commands.regions.whitelist.list_parent").replace("{list}", listText);


            // split by lines in cae
            String[] lines = fullMessage.split("\n");
            sender.sendMessage(lines);
        }
        else/* if(args.length >= 3)*/ {
            String regionName = args[1];
            String identifier = args[2];
            UUID uuid = getTargetByIdentifier(identifier);

            Region region = api.getRegion(regionName);

            if (region == null) {
                sender.sendMessage(api.getMessage("commands.regions.unknown_region").replace("{region}", regionName));
                return;
            }

            if (uuid == null) {
                sender.sendMessage(api.getMessage("commands.regions.unknown_identifier").replace("{player}", identifier));
                return;
            }

            if (region.isWhitelisted(uuid)) {
                region.removeWhitelistedEntity(uuid);
                sender.sendMessage(api.getMessage("commands.regions.whitelist.removed_player").replace("{player}", identifier));
            }
            else {
                sender.sendMessage(api.getMessage("commands.regions.whitelist.added_player").replace("{player}", identifier));
                region.addWhitelistedEntity(uuid);
            }
        }
    }

    private void handleFlagCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission(RegionAPI.modifyFlagPermission)) {
            sender.sendMessage(api.getMessage("commands.insufficient_permissions"));
            return;
        }

        // redirect to gui if only region is specified
        if (sender instanceof Player player && args.length == 2) {
            String regionName = args[1];
            Region region = api.getRegion(regionName);
            if (region == null) {
                sender.sendMessage(api.getMessage("commands.regions.unknown_region").replace("{region}", regionName));
                return;
            }

            FlagGUI.openFlagMenu(player, region);
        }
        else {

            if (args.length < 4) {
                sender.sendMessage("Usage: /region flag <name> <flag> <state>");
                return;
            }
            String regionName = args[1];
            String flagName = args[2];
            String scopeName = args[3];

            Region region = api.getRegion(regionName);
            RegionFlag flag = api.getFlag(flagName);
            FlagScope scope = FlagScope.getOrNull(scopeName);

            if (region == null) {
                sender.sendMessage(api.getMessage("commands.regions.unknown_region").replace("{region}", regionName));
                return;
            }
            if (flag == null) {
                sender.sendMessage(api.getMessage("commands.regions.unknown_flag").replace("{flag}", flagName));
                return;
            }
            if (scope == null) {
                sender.sendMessage(api.getMessage("commands.regions.unknown_flagscope").replace("{scope}", scopeName));
                return;
            }

            region.setFlag(flag, scope);
            sender.sendMessage(api.getMessage("commands.regions.flag.success").replace("{region}", region.getName()).replace("{flag}", flag.getName()).replace("{scope}", scope.toString().toLowerCase()));

        }

    }

    private void handleDefaultCommand(CommandSender sender, String[] args) {
        String regionName = args[0];
        Region region = api.getRegion(regionName);

        if (region == null) {
            sender.sendMessage(api.getMessage("commands.regions.unknown_region").replace("{region}", regionName));
            return;
        }

        if(sender instanceof  Player player) {
            RegionManagerGUI.openManagerMenu(player, region);
        }
        else{
            sender.sendMessage("Only players can open gui menus.");
        }
    }

    private List<String> predictSuggestionStartingWith(String prefix, Collection<String> col) {
        List<String> list = new ArrayList<>(col);
        List<String> valid = new ArrayList<>();

        if (prefix != null && !prefix.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).toLowerCase().startsWith(prefix.toLowerCase(

                ))) {
                    valid.add(list.get(i));
                }
            }
        }
        else {
            return list;
        }

        //sort alphabetically
        valid.sort(String::compareTo);

        return valid;
    }

    private List<String> getPlayersAndTargetSuggestion(CommandSender sender) {
        List<String> list = new ArrayList<>();

        for (Player plyr : sender.getServer().getOnlinePlayers()) {
            list.add(plyr.getName());
        }

        if (sender instanceof Player plyr) {
            Entity target = plyr.getTargetEntity(5);
            if (target != null) {
                list.add(target.getUniqueId().toString());
            }
        }

        return list;
    }

    private UUID getTargetByIdentifier(String identifier) {
        // might be a username
        Entity entity = Bukkit.getServer().getPlayer(identifier);
        if (entity != null) {
            return entity.getUniqueId();
        }

        try {
            UUID uuid = UUID.fromString(identifier);

            // must be an offline player or an entity
            return uuid;

        }
        catch (IllegalArgumentException e) {
            // not a username or uuid.
            return null;
        }


    }
}
