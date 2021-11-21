package dev.mayuna.mayusjdautils.utils;

import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emote;

public enum SystemEmotes {

    WINE("wine", 864203521595146260L),
    CLOUD("cloudicon", 864203519771148308L),
    KEY("keyicon", 864203521325924374L),
    WARNING("warning_", 815232719713337395L),
    TERMINAL("terminal", 815232719821865041L),
    SUCCESS("mark", 815232720052944926L),
    ANNOUNCE("announce", 864203519376621639L),
    BELL("bell_", 815232719977185330L),
    TEXT("text", 864203520525598761L),
    GIFT("gifticon", 864203521612316703L),
    BLOCKED("blocked", 864203521838678026L),
    LIGHT("light", 864203521209139272L),
    WATER_DROP("water", 815232720220848138L),
    DATABASE("database", 864203521497759804L),
    INFORMATION("info", 815232720090824725L),
    ERROR("error", 815232206511013898L),
    SETTINGS("settings", 864203520219414558L),
    MESSAGE("message", 864203519871025173L),
    KEYS("keys", 864203521750597662L);

    private @Getter final String emoteName;
    private @Getter final long emoteId;

    SystemEmotes(String emoteName, long emoteId) {
        this.emoteName = emoteName;
        this.emoteId = emoteId;
    }

    public Emote getAsEmote(JDA jda) {
        return jda.getEmoteById(emoteId);
    }

    @Override
    public String toString() {
        return "<:" + emoteName + ":" + emoteId + ">";
    }
}
