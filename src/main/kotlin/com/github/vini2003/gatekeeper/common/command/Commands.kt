/**
 * Copyright 2020 vini2003
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.github.vini2003.gatekeeper.common.command

import com.github.vini2003.gatekeeper.Gatekeeper
import com.github.vini2003.gatekeeper.accessor.PlayerLoginAccessor
import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.arguments.StringArgumentType.word
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralText
import net.minecraft.util.Formatting
import org.apache.logging.log4j.Level

object Commands {
	fun initialize() {
		/**
		 * Registers an user in the database,
		 * failing if:
		 *
		 * - The user is already registered;
		 * - An internal error has occurred;
		 */
		CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher, _ ->
			CommandManager.literal("register")
					.then(argument("password", word())
							.executes { context: CommandContext<ServerCommandSource> ->
								Gatekeeper.DATABASE.also { database ->
									context.source.player.uuid.also { uuid ->
										if (!database.contains(uuid)) {
											getString(context, "password").also { password ->
												database.register(uuid, password)

												context.source.player.sendMessage(LiteralText("Registration successful: you are registered.").formatted(Formatting.GREEN), false)
												Gatekeeper.LOGGER.info("${context.source.player.name.string}:${context.source.player.uuid} registration succeeded!")

												if (database.login(uuid, password)) {
													(context.source.player as PlayerLoginAccessor).gatekeeper_setLoggedIn(true)
													(context.source.player as PlayerLoginAccessor).gatekeeper_setLoginInTime(400)

													context.source.player.sendMessage(LiteralText("Post-registration login successful: you are free to play.").formatted(Formatting.GREEN), false)
													Gatekeeper.LOGGER.info("${context.source.player.name.string}:${context.source.player.uuid} post-registration login succeeded!")
												} else {
													context.source.player.sendMessage(LiteralText("Post-registration login failed: report this to an administrator.").formatted(Formatting.RED), false)
													Gatekeeper.LOGGER.log(Level.ERROR, "${context.source.player.name.string}:${context.source.player.uuid} registration failed: internal failure!")
												}
											}
										} else {
											context.source.player.sendMessage(LiteralText("Registration failed: you are already registered.").formatted(Formatting.GOLD), false)
											Gatekeeper.LOGGER.info("${context.source.player.name.string}:${context.source.player.uuid} registration failed: user already registered!")
										}
									}
								}

								1
							}).build().also {
						dispatcher.root.addChild(it)
					}
		})

		/**
		 * Logs an user in, failing if:
		 *
		 * - The user is already logged in;
		 * - The incorrect password was entered;
		 * - The user is not registered;
		 * - An internal error has occurred;
		 *
		 * After three failed login attempts,
		 * the user will be disconnected.
		 */
		CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher, _ ->
			CommandManager.literal("login")
					.then(argument("password", word())
							.executes { context: CommandContext<ServerCommandSource> ->
								Gatekeeper.DATABASE.also { database ->
									context.source.player.uuid.also { uuid ->
										if ((context.source.player as PlayerLoginAccessor).gatekeeper_isLoggedIn()) {
											context.source.player.sendMessage(LiteralText("Login failed: you are already logged in.").formatted(Formatting.GOLD), false)
											Gatekeeper.LOGGER.info("${context.source.player.name.string}:${context.source.player.uuid} login failed: user already logged in!")
										} else if (database.contains(uuid)) {
											getString(context, "password").also { password ->
												if (database.login(uuid, password)) {
													(context.source.player as PlayerLoginAccessor).gatekeeper_setLoggedIn(true)
													(context.source.player as PlayerLoginAccessor).gatekeeper_setLoginInTime(400)

													context.source.player.sendMessage(LiteralText("Login successful: you are free to play.").formatted(Formatting.GREEN), false)
													Gatekeeper.LOGGER.info("${context.source.player.name.string}:${context.source.player.uuid} login succeeded!")
												} else {
													context.source.player.sendMessage(LiteralText("Login failed: you entered the wrong password.").formatted(Formatting.RED), false)
													Gatekeeper.LOGGER.info("${context.source.player.name.string}:${context.source.player.uuid} login failed: user entered the wrong password!")

													(context.source.player as PlayerLoginAccessor).gatekeeper_getLoginAttempts().also { attempts ->
														if (attempts < 3) {
															context.source.player.sendMessage(LiteralText("${3 - attempts} attempt${if (attempts == 2) "" else "s"} remaining.").formatted(Formatting.GOLD), false)
															Gatekeeper.LOGGER.info("${context.source.player.name.string}:${context.source.player.uuid} login failed: ${3 - attempts} attempt${if (attempts == 2) "" else "s"} remaining!")

															(context.source.player as PlayerLoginAccessor).gatekeeper_setLoginAttempts(attempts + 1)
														} else if (attempts >= 3 && context.source.player is ServerPlayerEntity) {
															(context.source.player as ServerPlayerEntity).networkHandler.disconnect(LiteralText("Login attempts exceeded maximum threshold.").formatted(Formatting.GOLD))
															Gatekeeper.LOGGER.info("${context.source.player.name.string}:${context.source.player.uuid} login failed: user exceeded maximum login attempt threshold!")
														}
													}
												}
											}
										} else {
											context.source.player.sendMessage(LiteralText("Login failed: you are not registered.").formatted(Formatting.RED), false)
											Gatekeeper.LOGGER.info("${context.source.player.name.string}:${context.source.player.uuid} login failed: user not registered!")
										}
									}
								}

								1
							}).build().also {
						dispatcher.root.addChild(it)
					}
		})

		Gatekeeper.LOGGER.info("Commands initialized.")
	}
}