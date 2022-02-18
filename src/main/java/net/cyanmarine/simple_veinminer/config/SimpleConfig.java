package net.cyanmarine.simple_veinminer.config;

import me.lortseam.completeconfig.api.ConfigContainer;
import me.lortseam.completeconfig.api.ConfigEntries;
import me.lortseam.completeconfig.api.ConfigEntry;
import me.lortseam.completeconfig.api.ConfigGroup;
import me.lortseam.completeconfig.data.Config;
import me.lortseam.completeconfig.extension.BaseExtension;
import net.cyanmarine.simple_veinminer.SimpleVeinminer;
import net.cyanmarine.simple_veinminer.server.SimpleVeinminerServer;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SimpleConfig extends Config implements ConfigContainer {

    @Transitive
    public Restrictions restrictions = new Restrictions();
    @Transitive
    public Exhaustion exhaustion = new Exhaustion();
    @Transitive
    public Durability durability = new Durability();

    @ConfigEntry
    public int maxBlocks = 150;

    public SimpleConfig() {
        super("simple_veinminer");
    }

    @ConfigEntries
    public static class Restrictions implements ConfigGroup {
        public boolean canVeinmineHungry = false;
        public boolean canVeinmineWithEmptyHand = true;
        public boolean creativeBypass = true;
        @ConfigEntry(tooltipTranslationKeys = {"tooltip.suitableTools.1", "tooltip.suitableTools.2"}, comment = "Will only allow to veinmine wood using an axe, dirt using a shovel, stone using a pickaxe, etc.")
        public boolean canOnlyUseSuitableTools = false;

        public void setCanVeinmineWithEmptyHand(boolean canVeinmineWithEmptyHand) {  this.canVeinmineWithEmptyHand = canVeinmineWithEmptyHand; syncConfig(); }
        public void setCreativeBypass(boolean creativeBypass) {  this.creativeBypass = creativeBypass; syncConfig(); }
        public void setCanVeinmineHungry(boolean canVeinmineHungry) {this.canVeinmineHungry = canVeinmineHungry; syncConfig();}
        public void setCanOnlyUseSuitableTools(boolean canOnlyUseSuitableTools) {this.canOnlyUseSuitableTools = canOnlyUseSuitableTools; syncConfig();}

        @Transitive
        public RestrictionList restrictionList = new RestrictionList();

        @ConfigEntries
        public static class RestrictionList implements ConfigGroup {
            @ConfigEntry(comment = "Valid values are NONE, BLACKLIST, and WHITELIST")
            @ConfigEntry.Dropdown
            public ListType listType = ListType.NONE;
            @ConfigEntry(comment = "More information at https://github.com/PrincessCyanMarine/Simple-Veinminer/wiki/Whitelist-and-Blacklist")
            public List<String> list = Arrays.asList("#minecraft:logs", "#c:ores");

            public void setList(List<String> list) {this.list = list; syncConfig();}
            public void setListType(ListType listType) {this.listType = listType; syncConfig();}

            public static enum ListType {
                NONE, WHITELIST, BLACKLIST;
            }
        }

    }

    @ConfigEntries
    public static class Exhaustion implements ConfigGroup {
        public double baseValue = 0.3;
        public boolean exhaustionBasedOnHardness = true;
        public double hardnessWeight = 0.1;
    }

    @ConfigEntries
    public static class Durability implements ConfigGroup {
        public double damageMultiplier = 1.0;
        public double swordMultiplier = 2.0;
        public boolean consumeOnInstantBreak = false;
    }

    public static class SimpleConfigCopy {
        public Restrictions restrictions = new Restrictions();
        public int maxBlocks = 150;

        private SimpleConfigCopy() {}

        SimpleConfigCopy(PacketByteBuf buf) {
            this.restrictions.canVeinmineHungry = buf.readBoolean();
            this.restrictions.canVeinmineWithEmptyHand = buf.readBoolean();
            this.restrictions.creativeBypass = buf.readBoolean();
            this.restrictions.canOnlyUseSuitableTools = buf.readBoolean();
            this.restrictions.restrictionList.listType = buf.readEnumConstant(Restrictions.RestrictionList.ListType.class);
            this.restrictions.restrictionList.list = Arrays.stream(buf.readString().split(";")).toList();

            this.maxBlocks = buf.readInt();
        }

        public static SimpleConfigCopy from(SimpleConfig config) {
            SimpleConfigCopy res = new SimpleConfigCopy();

            res.restrictions = config.restrictions;
            res.maxBlocks = config.maxBlocks;

            return res;
        }
    }

    public static SimpleConfigCopy copy(PacketByteBuf buf) { return new SimpleConfigCopy(buf); }

    public PacketByteBuf WritePacket() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(restrictions.canVeinmineHungry);
        buf.writeBoolean(restrictions.canVeinmineWithEmptyHand);
        buf.writeBoolean(restrictions.creativeBypass);
        buf.writeBoolean(restrictions.canOnlyUseSuitableTools);
        buf.writeEnumConstant(restrictions.restrictionList.listType);
        buf.writeString(String.join(";", restrictions.restrictionList.list));

        buf.writeInt(maxBlocks);

        return buf;
    }

    public static void syncConfig() { SimpleVeinminer.SyncConfigForAllPlayers(); }

    public void setMaxBlocks(int maxBlocks) {  this.maxBlocks = maxBlocks; syncConfig(); }
}
