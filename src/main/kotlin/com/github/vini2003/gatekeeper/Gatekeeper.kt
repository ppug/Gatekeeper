/**
 * Copyright 2020 vini2003
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.github.vini2003.gatekeeper

import com.github.vini2003.gatekeeper.common.callback.Callbacks
import com.github.vini2003.gatekeeper.common.command.Commands
import com.github.vini2003.gatekeeper.common.database.Database
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.util.Identifier
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class Gatekeeper : ModInitializer {
	companion object {
		@SuppressWarnings
		const val MOD_ID = "gatekeeper"

		@JvmStatic
		val LOGGER: Logger = LogManager.getLogger(MOD_ID)

		@JvmStatic
		val DATABASE: Database = Database()

		@JvmStatic
		fun identifier(string: String): Identifier {
			return Identifier(MOD_ID, string)
		}
	}

	@ExperimentalStdlibApi
	override fun onInitialize() {
		LOGGER.log(Level.INFO, "Initialization starting.")

		val path = "${FabricLoader.getInstance().configDir}/$MOD_ID/database.json"

		LOGGER.log(Level.INFO, "Utilizing database path '$path'.")

		ServerLifecycleEvents.SERVER_STARTED.register(ServerLifecycleEvents.ServerStarted {
			val before = System.currentTimeMillis()

			DATABASE.verify(path)
			DATABASE.deserialize(path)

			val now = System.currentTimeMillis()

			LOGGER.log(Level.INFO, "De-serialized database in ${now - before}ms.")
		})

		ServerLifecycleEvents.SERVER_STOPPED.register(ServerLifecycleEvents.ServerStopped {
			val before = System.currentTimeMillis()

			DATABASE.serialize(path)

			val now = System.currentTimeMillis()

			LOGGER.log(Level.INFO, "De-serialized database in ${now - before}ms.")
		})

		Commands.initialize()

		Callbacks.initialize()

		LOGGER.log(Level.INFO, "Initialization complete.")
	}
}