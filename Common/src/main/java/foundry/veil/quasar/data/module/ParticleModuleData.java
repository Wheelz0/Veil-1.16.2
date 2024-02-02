package foundry.veil.quasar.data.module;

import com.mojang.serialization.Codec;
import foundry.veil.quasar.client.particle.ParticleModuleSet;
import foundry.veil.quasar.client.particle.QuasarParticle;
import foundry.veil.quasar.data.DynamicParticleDataRegistry;
import foundry.veil.quasar.data.ParticleModuleTypeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;

public interface ParticleModuleData {

    Codec<ParticleModuleData> INIT_DIRECT_CODEC = ParticleModuleTypeRegistry.INIT_MODULE_CODEC
            .dispatch("module", ParticleModuleData::getType, ModuleType::codec);
    Codec<ParticleModuleData> UPDATE_DIRECT_CODEC = ParticleModuleTypeRegistry.UPDATE_MODULE_CODEC
            .dispatch("module", ParticleModuleData::getType, ModuleType::codec);
    Codec<ParticleModuleData> RENDER_DIRECT_CODEC = ParticleModuleTypeRegistry.RENDER_MODULE_CODEC
            .dispatch("module", ParticleModuleData::getType, ModuleType::codec);

    Codec<Holder<ParticleModuleData>> INIT_CODEC = RegistryFileCodec.create(DynamicParticleDataRegistry.INIT_MODULES, INIT_DIRECT_CODEC);
    Codec<Holder<ParticleModuleData>> UPDATE_CODEC = RegistryFileCodec.create(DynamicParticleDataRegistry.UPDATE_MODULES, UPDATE_DIRECT_CODEC);
    Codec<Holder<ParticleModuleData>> RENDER_CODEC = RegistryFileCodec.create(DynamicParticleDataRegistry.RENDER_MODULES, RENDER_DIRECT_CODEC);

    void registerModules(QuasarParticle particle, ParticleModuleSet.Builder registry);

    ModuleType<?> getType();
}
