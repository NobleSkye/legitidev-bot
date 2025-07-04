package net.legitimoose.bot.discord

import FindCommand
import ListCommand
import MsgCommand
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.requests.GatewayIntent
import net.legitimoose.bot.LegitimooseBot.config
import net.minecraft.client.Minecraft

class DiscordBot : ListenerAdapter() {
        companion object {
                lateinit var jda: JDA
                fun runBot() {
                        jda =
                                JDABuilder.createDefault(config.discordToken)
                                        .enableIntents(
                                                GatewayIntent.MESSAGE_CONTENT,
                                                GatewayIntent.GUILD_MEMBERS
                                        )
                                        .build()

                        jda.addEventListener(DiscordBot())
                        jda.updateCommands()
                                .addCommands(
                                        Commands.slash("list", "List online players in the server")
                                                .addOption(
                                                        OptionType.BOOLEAN,
                                                        "lobby",
                                                        "True if you only want to see online players in the lobby"
                                                ),
                                        Commands.slash("find", "Find which world a player is in")
                                                .addOption(
                                                        OptionType.STRING,
                                                        "player",
                                                        "The username of the player you want to find",
                                                        true
                                                ),
                                        Commands.slash("msg", "Message an ingame player")
                                                .addOption(
                                                        OptionType.STRING,
                                                        "player",
                                                        "The username of the player you want to message",
                                                        true
                                                )
                                                .addOption(
                                                        OptionType.STRING,
                                                        "message",
                                                        "The message you want to send",
                                                        true
                                                )
                                )
                                .queue()
                }
        }

        override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
                when (event.name) {
                        "list" ->
                                ListCommand(event, event.getOption("lobby")?.asBoolean)
                                        .onCommandReceived()
                        "find" ->
                                FindCommand(event, event.getOption("player")!!.asString)
                                        .onCommandReceived()
                        "msg" ->
                                MsgCommand(
                                                event,
                                                event.getOption("message")!!.asString,
                                                event.getOption("player")!!.asString
                                        )
                                        .onCommandReceived()
                }
        }

        override fun onMessageReceived(event: MessageReceivedEvent) {
                if (event.isWebhookMessage || event.author.isBot) return
                val discordNick = event.member!!.effectiveName.replace("§", "?")
                var message =
                        "<br><blue><b>ᴅɪsᴄᴏʀᴅ</b></blue> <yellow>$discordNick</yellow><dark_gray>:</dark_gray> " +
                                event.message
                                        .contentStripped
                                        .replace("\n", "<br>")
                                        .replace("§", "?")
                if (event.message.attachments.size != 0) {
                        message += " <blue>[Attachment Included]</blue>"
                }
                if (message.length >= 200) return
                if (event.channel.id == config.channelId) {
                        Minecraft.getInstance().player?.connection?.sendChat("$message")
                }
        }
}
