package dev.mayuna.mayusjdautils.util;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.sharding.ShardManager;

/**
 * Few {@link SystemEmote}s that I use in my projects. You can change their name and ID to your liking/custom emotes.
 */
public class SystemEmote {

    public static final SystemEmote WINE = new SystemEmote("wine", 864203521595146260L);
    public static final SystemEmote CLOUD = new SystemEmote("cloudicon", 864203519771148308L);
    public static final SystemEmote KEY = new SystemEmote("keyicon", 864203521325924374L);
    public static final SystemEmote WARNING = new SystemEmote("warning_", 815232719713337395L);
    public static final SystemEmote TERMINAL = new SystemEmote("terminal", 815232719821865041L);
    public static final SystemEmote SUCCESS = new SystemEmote("mark", 815232720052944926L);
    public static final SystemEmote ANNOUNCE = new SystemEmote("announce", 864203519376621639L);
    public static final SystemEmote BELL = new SystemEmote("bell_", 815232719977185330L);
    public static final SystemEmote TEXT = new SystemEmote("text", 864203520525598761L);
    public static final SystemEmote GIFT = new SystemEmote("gifticon", 864203521612316703L);
    public static final SystemEmote BLOCKED = new SystemEmote("blocked", 864203521838678026L);
    public static final SystemEmote LIGHT = new SystemEmote("light", 864203521209139272L);
    public static final SystemEmote WATER_DROP = new SystemEmote("water", 815232720220848138L);
    public static final SystemEmote DATABASE = new SystemEmote("database", 864203521497759804L);
    public static final SystemEmote INFORMATION = new SystemEmote("info", 815232720090824725L);
    public static final SystemEmote ERROR = new SystemEmote("error", 815232206511013898L);
    public static final SystemEmote SETTINGS = new SystemEmote("settings", 864203520219414558L);
    public static final SystemEmote MESSAGE = new SystemEmote("message", 864203519871025173L);
    public static final SystemEmote KEYS = new SystemEmote("keys", 864203521750597662L);

    private @Getter @Setter String name;
    private @Getter @Setter long id;

    /**
     * Constructs {@link SystemEmote} from name and ID
     * @param name Name
     * @param id ID
     */
    public SystemEmote(String name, long id) {
        this.name = name;
        this.id = id;
    }

    /**
     * Returns the emoji as a RichCustomEmoji if it is a custom emoji, otherwise returns null.
     *
     * @param jda The JDA instance to use to retrieve the emoji.
     *
     * @return A RichCustomEmoji object
     */
    public RichCustomEmoji getAsEmoji(JDA jda) {
        return jda.getEmojiById(id);
    }

    /**
     * Returns the emoji as a RichCustomEmoji object if it is a custom emoji, otherwise returns null.
     *
     * @param shardManager The shard manager to use to get the emoji from.
     *
     * @return A RichCustomEmoji object.
     */
    public RichCustomEmoji getAsEmoji(ShardManager shardManager) {
        return shardManager.getEmojiById(id);
    }

    /**
     * It returns the emoji as a mention string: <:name:id>
     *
     * @return The toString() method is being returned.
     */
    public String getAsMention() {
        return toString();
    }

    @Override
    public String toString() {
        return "<:" + name + ":" + id + ">";
    }
}
