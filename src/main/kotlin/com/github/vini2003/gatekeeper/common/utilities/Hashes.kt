/**
 * Copyright 2020 vini2003
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.github.vini2003.gatekeeper.common.utilities

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom

class Hashes {
	companion object {
		/**
		 * Hash a string with SHA-256 and a salt.
		 */
		fun hashAndSalt(string: String, salt: ByteArray): SaltedHashData {
			MessageDigest.getInstance("SHA-256").also { digest ->
				digest.update(salt)

				return SaltedHashData(digest.digest(string.toByteArray(StandardCharsets.UTF_8)), salt)
			}
		}

		/**
		 * Hash a string with SHA-256 and a 32-byte salt.
		 */
		fun hashAndSalt(string: String): SaltedHashData {
			ByteArray(32).also { salt ->
				SecureRandom().also { random ->
					random.nextBytes(salt)
				}

				return hashAndSalt(string, salt)
			}
		}
	}

	/**
	 * Holds a salted SHA-256 hash, and the salt used in the procedure.
	 */
	data class SaltedHashData(val hash: ByteArray, val salt: ByteArray) {
		override fun equals(other: Any?): Boolean {
			if (this === other) return true
			if (other !is SaltedHashData) return false

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