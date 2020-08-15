/**
 * Copyright 2020 vini2003
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.github.vini2003.gatekeeper.common.database

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.github.vini2003.gatekeeper.common.utilities.Hashes
import org.apache.commons.codec.binary.Hex
import java.io.File
import java.util.*

class Database {
	private val users: MutableMap<UUID, User> = mutableMapOf()

	/**
	 * Inspects the database at the specified file,
	 * creating it if it does not exist.
	 *
	 * Does NOT check for database integrity,
	 * such is verified during de-serialization.
	 */
	fun verify(path: String) {
		if (File(path).exists()) {
			return
		} else {
			File(path).also { file ->
				file.parentFile.mkdirs()

				file.outputStream().also { stream ->
					stream.bufferedWriter().also { buffer ->
						buffer.write("{\n}")

						buffer.close()
					}

					stream.close()
				}
			}
		}
	}

	/**
	 * Deserializes the database from the specified file.
	 */
	@ExperimentalStdlibApi
	fun deserialize(path: String) {
		File(path).inputStream().also { stream ->
			(Parser.default().parse(stream) as JsonObject).also { data ->
				data.forEach { uuid, json ->
					users[UUID.fromString(uuid)] = User().also { it.fromJson(json as JsonObject) }
				}
			}

			stream.close()
		}
	}

	/**
	 * Serializes the database to the specified file.
	 */
	@ExperimentalStdlibApi
	fun serialize(path: String) {
		if (users.isEmpty()) {
			File(path).bufferedWriter().also { writer ->
				writer.write("{\n}")

				writer.close()
			}
		}
		JsonObject().also { data ->
			users.forEach { (uuid, user) ->
				data[uuid.toString()] = user.toJson()
			}

			File(path).outputStream().also { stream ->
				stream.bufferedWriter().also { writer ->
					writer.write(data.toJsonString(true))

					writer.close()
				}

				stream.close()
			}
		}
	}

	/**
	 * Asserts whether an user is in the database,
	 * returning TRUE if yes and FALSE if no.
	 */
	fun contains(uuid: UUID): Boolean {
		return users.containsKey(uuid)
	}

	/**
	 * Registers an user in the database.
	 */
	fun register(uuid: UUID, password: String) {
		users[uuid] = User().also { user ->
			Hashes.hashAndSalt(password).also { data ->
				user.hash = data.hash
				user.salt = data.salt
			}
		}
	}

	/**
	 * Attempts to log an user in, returning
	 * TRUE if successful and FALSE if not.
	 */
	fun login(uuid: UUID, password: String): Boolean {
		users[uuid].also { user ->
			if (user != null) {
				Hashes.hashAndSalt(password, user.salt).also { data ->
					return user.hash.contentEquals(data.hash)
				}
			} else {
				return false
			}
		}
	}

	/**
	 * Holds a salted SHA-256 hash, and the salt used in the procedure,
	 * providing serialization methods.
	 */
	class User {
		lateinit var hash: ByteArray
		lateinit var salt: ByteArray

		@ExperimentalStdlibApi
		fun toJson(): JsonObject {
			return JsonObject().also {
				it["hash"] = Hex.encodeHex(hash).concatToString()
				it["salt"] = Hex.encodeHex(salt).concatToString()
			}
		}

		@ExperimentalStdlibApi
		fun fromJson(json: JsonObject) {
			hash = Hex.decodeHex(json.string("hash")!!.toCharArray())
			salt = Hex.decodeHex(json.string("salt")!!.toCharArray())
		}

		override fun equals(other: Any?): Boolean {
			if (this === other) return true
			if (other !is User) return false

			if (!hash.contentEquals(other.hash)) return false
			if (!salt.contentEquals(other.salt)) return false

			return true
		}

		override fun hashCode(): Int {
			var result = hash.contentHashCode()
			result = 31 * result + salt.contentHashCode()
			return result
		}
	}
}