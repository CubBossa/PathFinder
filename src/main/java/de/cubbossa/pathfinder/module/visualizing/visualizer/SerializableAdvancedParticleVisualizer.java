package de.cubbossa.pathfinder.module.visualizing.visualizer;

import de.cubbossa.pathfinder.module.visualizing.VisualizerHandler;
import de.cubbossa.pathfinder.module.visualizing.VisualizerType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.*;
import java.util.function.Function;
import java.util.function.Supplier;

@Getter
@Setter
public class SerializableAdvancedParticleVisualizer extends AdvancedParticleVisualizer<SerializableAdvancedParticleVisualizer> {

    private String particleFunction = "step % 2 === 0 ? FLAME : SOUL_FIRE_FLAME";
    private String particleDataFunction = "";
    private String speedFunction = "0.0001";
    private String amountFunction = "1";
    private String particleOffsetXFunction = "0.0001";
    private String particleOffsetYFunction = "0.0001";
    private String particleOffsetZFunction = "0.0001";
    private String pathOffsetXFunction = "Math.sin(step)";
    private String pathOffsetYFunction = "Math.cos(step)";
    private String pathOffsetZFunction = "0";

    private ScriptEngine engine;

    public SerializableAdvancedParticleVisualizer(NamespacedKey key, String nameFormat) {
        super(key, nameFormat);

        engine = new NashornScriptEngineFactory().getScriptEngine("JavaScript");
        setParticle(context -> {
            Bindings bindings = contextBindings(engine, context);
            for (Particle particle : Particle.values()) {
                bindings.put(particle.toString(), particle);
            }
            try {
                return (Particle) engine.eval(particleFunction, bindings);
            } catch (ScriptException e) {
                throw new RuntimeException(e);
            }
        });
        setParticleData(context -> {
            Bindings bindings = contextBindings(engine, context);
            try {
                return engine.eval(particleDataFunction, bindings);
            } catch (ScriptException e) {
                throw new RuntimeException(e);
            }
        });
        setAmount(context -> {
            Bindings bindings = contextBindings(engine, context);
            try {
                return (int) engine.eval(amountFunction, bindings);
            } catch (ScriptException e) {
                throw new RuntimeException(e);
            }
        });
        setSpeed(resolveFloat(() -> speedFunction));
        setParticleOffsetX(resolveFloat(() -> particleOffsetXFunction));
        setParticleOffsetY(resolveFloat(() -> particleOffsetYFunction));
        setParticleOffsetZ(resolveFloat(() -> particleOffsetZFunction));
        setPathOffsetX(resolveFloat(() -> pathOffsetXFunction));
        setPathOffsetY(resolveFloat(() -> pathOffsetYFunction));
        setPathOffsetZ(resolveFloat(() -> pathOffsetZFunction));
    }

    private Function<Context, Float> resolveFloat(Supplier<String> script) {
        return context -> {
            Bindings bindings = contextBindings(engine, context);
            try {
                Object val = engine.eval(script.get(), bindings);
                if (val instanceof Integer integer) {
                    return (float) integer.floatValue();
                } else if (val instanceof Double dNumber) {
                    return (float) dNumber.floatValue();
                } else if (val instanceof Float fNumber) {
                    return (float) fNumber;
                }
                throw new RuntimeException(val.getClass().getName());
            } catch (ScriptException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private Bindings contextBindings(ScriptEngine engine, Context context) {
        Bindings bindings = engine.createBindings();
        bindings.put("playerdistance", context.player().getLocation().distance(context.point()));
        bindings.put("count", context.count());
        bindings.put("index", context.index());
        bindings.put("step", context.step());
        bindings.put("interval", context.interval());
        return bindings;
    }

    @Override
    public VisualizerType<SerializableAdvancedParticleVisualizer> getType() {
        return VisualizerHandler.ADV_PARTICLE_VISUALIZER_TYPE;
    }
}
