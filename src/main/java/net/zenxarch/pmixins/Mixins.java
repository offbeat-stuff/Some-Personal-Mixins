package net.zenxarch.pmixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.loot.condition.KilledByPlayerLootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;

public class Mixins {
  @Mixin(MobEntity.class)
  static class MobMixin{
    @Inject(
      method = "isAffectedByDaylight",
      at = @At("HEAD"),
      cancellable = true
    )
    void dontBurn(CallbackInfoReturnable<Boolean> cir){
      cir.setReturnValue(false);
    }
  }

  @Mixin(PlayerEntity.class)
  static class PlayerMixin {
    @Redirect(
      method = "dropInventory",
      at = @At(value = "INVOKE",
      target = "net/minecraft/world/GameRules.getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
	  boolean checkPlayerKillInventory(GameRules rules, GameRules.Key<GameRules.BooleanRule> key) {
		  return rules.getBoolean(key) && (((PlayerEntity) (Object) this).getAttacker() == null || ((PlayerEntity) (Object) this).getAttacker().getType() != EntityType.PLAYER);
	  }

	  @Redirect(
      method = "getXpToDrop",
      at = @At(value = "INVOKE",
      target = "net/minecraft/world/GameRules.getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
	  boolean checkPlayerKillXP(GameRules rules, GameRules.Key<GameRules.BooleanRule> key) {
		  return rules.getBoolean(key) && (((PlayerEntity) (Object) this).getAttacker() == null || ((PlayerEntity) (Object) this).getAttacker().getType() != EntityType.PLAYER);
	  }
  }

  @Mixin(ServerPlayerEntity.class)
  static class ServerPlayerMixin {
    @Redirect(
      method = "copyFrom",
      at = @At(value = "INVOKE",
      target = "net/minecraft/world/GameRules.getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
    public boolean onCopyFrom(GameRules rules, GameRules.Key<GameRules.BooleanRule> key, ServerPlayerEntity oldPlayer) {
        return rules.getBoolean(key) && (oldPlayer.getAttacker() == null || oldPlayer.getAttacker().getType() != EntityType.PLAYER);
    }
  }

  @Mixin(LivingEntity.class)
    static class LivingMixin {
        @Inject(at = @At("HEAD"), method = "shouldAlwaysDropXp", cancellable = true)
        void dropXP(CallbackInfoReturnable<Boolean> cir){
            cir.setReturnValue(true);
        }
    }

    @Mixin(KilledByPlayerLootCondition.class)
    static class PlayerLootMixin {
        @Inject(at = @At("HEAD"), method = "test", cancellable = true)
        void dropPlayerLoot(LootContext lootContext, CallbackInfoReturnable<Boolean> cir){
            cir.setReturnValue(true);
        }
    }
}
