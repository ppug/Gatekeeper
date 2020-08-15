/**
 * Copyright 2020 vini2003
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.github.vini2003.gatekeeper.mixin;

import com.github.vini2003.gatekeeper.accessor.PlayerLoginAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements PlayerLoginAccessor {
	@Shadow
	public abstract void sendMessage(Text message, boolean actionBar);

	boolean gatekeeper_isLoggedIn = false;

	long gatekeeper_logInTime = 400;

	int gatekeeper_loginAttempts = 0;

	Vec3d previousPosition = ((Entity) (Object) this).getPos();

	@Override
	public boolean gatekeeper_isLoggedIn() {
		return gatekeeper_isLoggedIn;
	}

	@Override
	public void gatekeeper_setLoggedIn(boolean gatekeeper_isLoggedIn) {
		this.gatekeeper_isLoggedIn = gatekeeper_isLoggedIn;
	}

	@Override
	public void gatekeeper_setLoginInTime(long gatekeeper_logInTime) {
		this.gatekeeper_logInTime = gatekeeper_logInTime;
	}

	@Override
	public int gatekeeper_getLoginAttempts() {
		return gatekeeper_loginAttempts;
	}

	@Override
	public void gatekeeper_setLoginAttempts(int gatekeeper_loginAttempts) {
		this.gatekeeper_loginAttempts = gatekeeper_loginAttempts;
	}

	/**
	 * Prompts the user to login or register,
	 * disconnecting them after 20 seconds
	 * without login.
	 */
	@Inject(at = @At("HEAD"), method = "tick")
	void onTickHead(CallbackInfo ci) {
		if (!gatekeeper_isLoggedIn && (Object) this instanceof ServerPlayerEntity) {
			previousPosition = ((Entity) (Object) this).getPos();

			if (gatekeeper_logInTime == 400) {
				this.sendMessage(new LiteralText("Login with /login <password>, or register yourself with /register <password>.").formatted(Formatting.GREEN), false);
			} else if (gatekeeper_logInTime == 200) {
				this.sendMessage(new LiteralText("10 seconds remaining until disconnection!").formatted(Formatting.YELLOW), false);
			} else if (gatekeeper_logInTime == 100) {
				this.sendMessage(new LiteralText("5 seconds remaining until disconnection!").formatted(Formatting.YELLOW), false);
			} else if (gatekeeper_logInTime == 80) {
				this.sendMessage(new LiteralText("4 seconds remaining until disconnection!").formatted(Formatting.YELLOW), false);
			} else if (gatekeeper_logInTime == 60) {
				this.sendMessage(new LiteralText("3 seconds remaining until disconnection!").formatted(Formatting.GOLD), false);
			} else if (gatekeeper_logInTime == 40) {
				this.sendMessage(new LiteralText("2 seconds remaining until disconnection!").formatted(Formatting.GOLD), false);
			} else if (gatekeeper_logInTime == 20) {
				this.sendMessage(new LiteralText("1 seconds remaining until disconnection!").formatted(Formatting.RED), false);
			} else if (gatekeeper_logInTime == 0) {
				this.sendMessage(new LiteralText("Login timed out: you will be disconnected.").formatted(Formatting.DARK_RED), false);
			} else if (gatekeeper_logInTime <= -20) {
				((ServerPlayerEntity) (Object) this).networkHandler.disconnect(new LiteralText("Login timed out.").formatted(Formatting.GOLD));
			}

			--gatekeeper_logInTime;
		}
	}

	@Inject(at = @At("RETURN"), method = "tick")
	void onTickReturn(CallbackInfo ci) {
		if (!gatekeeper_isLoggedIn && (Object) this instanceof ServerPlayerEntity) {
			((ServerPlayerEntity) (Object) this).teleport(previousPosition.x, previousPosition.y, previousPosition.z);
		}
	}
}
