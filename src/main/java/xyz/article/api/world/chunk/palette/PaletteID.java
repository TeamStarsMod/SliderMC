package xyz.article.api.world.chunk.palette;

import org.geysermc.mcprotocollib.protocol.data.game.chunk.BitStorage;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.DataPalette;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.palette.*;
import java.lang.reflect.Constructor;
import java.util.EnumMap;
import java.util.Map;

public enum PaletteID {
    GLOBAL(0, GlobalPalette.class),
    LIST(1, ListPalette.class, int.class),
    MAP(2, MapPalette.class, int.class),
    SINGLETON(3, SingletonPalette.class, int.class);

    private final int id;
    private final Class<? extends Palette> type;
    private final Class<?>[] parameterTypes;

    private static final Map<PaletteID, Constructor<? extends Palette>> CONSTRUCTOR_CACHE =
            new EnumMap<>(PaletteID.class);

    static {
        for (PaletteID paletteID : values()) {
            try {
                if (paletteID == GLOBAL) {
                    continue; // 单例不需要构造函数
                }
                Constructor<? extends Palette> constructor =
                        paletteID.type.getDeclaredConstructor(paletteID.parameterTypes);
                constructor.setAccessible(true);
                CONSTRUCTOR_CACHE.put(paletteID, constructor);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Missing constructor for " + paletteID.type, e);
            }
        }
    }

    PaletteID(int id, Class<? extends Palette> type, Class<?>... parameterTypes) {
        this.id = id;
        this.type = type;
        this.parameterTypes = parameterTypes;
    }

    public static PaletteID fromId(int id) {
        for (PaletteID type : values()) {
            if (type.id == id) return type;
        }
        return null;
    }

    public static PaletteID fromPalette(Palette palette) {
        for (PaletteID type : values()) {
            if (type.type.isInstance(palette)) return type;
        }
        return null;
    }

    public static DataPalette getPaletteFromID(int id, BitStorage bitStorage, PaletteType paletteType) {
        PaletteID paletteID = fromId(id);
        if (paletteID == null) {
            throw new IllegalArgumentException("Invalid palette id: " + id);
        }

        try {
            Palette instance;
            if (paletteID == GLOBAL) {
                instance = GlobalPalette.INSTANCE;
            } else {
                Constructor<? extends Palette> constructor = CONSTRUCTOR_CACHE.get(paletteID);
                Object[] args = new Object[constructor.getParameterCount()];
                for (int i = 0; i < args.length; i++) {
                    Class<?> paramType = constructor.getParameterTypes()[i];
                    if (paramType == int.class) {
                        if (paletteID == LIST || paletteID == MAP) {
                            args[i] = bitStorage.getBitsPerEntry();
                        } else if (paletteID == SINGLETON) {
                            // 假定SingletonPalette的State为0 (是因为目前不用，之后要写的)
                            args[i] = 0;
                        }
                    }
                }
                instance = constructor.newInstance(args);
            }
            return new DataPalette(instance, bitStorage, paletteType);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to instantiate palette: " + paletteID.type, e);
        }
    }
}