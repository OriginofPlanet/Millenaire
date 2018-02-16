package org.millenaire.building;

import com.google.gson.Gson;
import net.minecraft.util.ResourceLocation;
import org.millenaire.MillCulture;
import org.millenaire.util.ItemRateWrapper;
import org.millenaire.util.ResourceLocationUtil;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildingTypes {

    private static Map<ResourceLocation, BuildingType> buildingCache = new HashMap<>();

    public static void cacheBuildingTypes(MillCulture culture) {

        InputStream is = MillCulture.class.getClassLoader().getResourceAsStream("assets/millenaire/cultures/" + culture.cultureName.toLowerCase() + "/buildings/buildings.json");
        String[] buildings = new Gson().fromJson(new InputStreamReader(is), String[].class);

        for (String building : buildings) {
            ResourceLocation loc = new ResourceLocation(building);
            InputStream file = MillCulture.class.getClassLoader().getResourceAsStream("assets/millenaire/cultures/" + loc.getResourceDomain() + "/buildings/" + loc.getResourcePath() + ".json");
            BuildingType type = new Gson().fromJson(new InputStreamReader(file), BuildingType.class);
            buildingCache.put(loc, type);
        }
    }

    public static BuildingType getTypeByID(ResourceLocation rl) {
        return buildingCache.get(rl);
    }

    public static BuildingType getTypeFromProject(BuildingProject proj) {
        return buildingCache.get(ResourceLocationUtil.getRL(proj.ID));
    }

    public static Map<ResourceLocation, BuildingType> getCache() {
        return buildingCache;
    }

    public static class BuildingType {

        public boolean isTownHall = false;
        protected List<ItemRateWrapper> itemrates = new ArrayList<>();
        private String identifier;

        public BuildingType() {
        }

        public BuildingType(ResourceLocation cultureandname) {
            identifier = ResourceLocationUtil.getString(cultureandname);
        }

        public BuildingPlan loadBuilding() {
            ResourceLocation s = ResourceLocationUtil.getRL(identifier);
            try {
                return PlanIO.loadSchematic(PlanIO.getBuildingTag(s.getResourcePath(), MillCulture.getCulture(s.getResourceDomain()), true), MillCulture.getCulture(s.getResourceDomain()), 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
