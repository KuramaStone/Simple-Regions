package com.github.kuramastone.regionstrial.gui;

import com.github.kuramastone.regionstrial.RegionAPI;
import com.github.kuramastone.regionstrial.RegionPlugin;
import com.github.kuramastone.regionstrial.regions.Region;
import org.bukkit.ChatColor;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RenameConversation {

    private RegionAPI api;
    private Region region;

    public RenameConversation(Region region) {
        this.region = region;
        api = RegionPlugin.instance.getApi();
    }

    public void startConversation(Player player) {
        ConversationFactory factory = new ConversationFactory(RegionPlugin.instance)
                .withModality(true)  // Ensure the conversation blocks chat
                .withPrefix((context) -> ChatColor.GREEN + "[%s] ".formatted(RegionPlugin.instance.getName()) + ChatColor.RESET)
                .withFirstPrompt(new RenamePrompt())
                .withEscapeSequence("exit")  // The player can type "exit" to cancel the conversation
                .withEscapeSequence("cancel")  // The player can type "exit" to cancel the conversation
                .withEscapeSequence("nowaitididntmeantousethisplsreverseit")  // The player can type "exit" to cancel the conversation
                .withTimeout(10)  // Timeout in seconds
                .thatExcludesNonPlayersWithMessage("Only players can use this!");

        Conversation conversation = factory.buildConversation(player);
        conversation.begin();
    }

    private class RenamePrompt extends StringPrompt {

        @Override
        public @NotNull String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + "Enter new name for region: ";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            Player player = (Player) context.getForWhom();
            api.renameRegion(input, region);

            // instead of responding to the player, reopen the initial gui
            RegionManagerGUI.openManagerMenu(player, region);

            return Prompt.END_OF_CONVERSATION;
        }
    }


}
