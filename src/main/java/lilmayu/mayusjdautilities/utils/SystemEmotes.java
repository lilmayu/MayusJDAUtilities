package lilmayu.mayusjdautilities.utils;

public enum SystemEmotes {

    ERROR("<:error:815232206511013898>"),
    SUCCESSFUL("<:mark:815232720052944926>"),
    WARNING("<:warning_:815232719713337395>"),
    INFORMATION("<:info:815232720090824725>"),
    BELL("<:bell_:815232719977185330>"),
    WATER_DROP("<:water:815232720220848138>"),
    TERMINAL("<:terminal:815232719821865041>");

    public final String NAME;

    SystemEmotes(String name) {
        this.NAME = name;
    }

    @Override
    public String toString() {
        return NAME;
    }
}
