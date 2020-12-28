package net.pseudow.tools.npc;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.util.UUIDTypeAdapter;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GameProfileBuilder {
    private static final String JSON_SKIN = "{\"timestamp\":%d,\"profileId\":\"%s\",\"profileName\":\"%s\",\"isPublic\":true,\"textures\":{\"SKIN\":{\"url\":\"%s\"}}}";

    /**
     * Create a new GameProfile.
     *
     * @author Jofkos (https://gist.github.com/Jofkos/79af290e94acdc7d7d5b)
     *
     * @param uuid - The new UUID
     * @param name - The name of the new player
     * @param skinUrl - The skin url.
     * @return - A new GameProfile
     */
    public static GameProfile getProfile(UUID uuid, String name, String skinUrl) {
        GameProfile profile = new GameProfile(uuid, name);

        List<Object> args = new ArrayList<>();
        args.add(System.currentTimeMillis());
        args.add(UUIDTypeAdapter.fromUUID(uuid));
        args.add(name);
        args.add(skinUrl);

        profile.getProperties().put("textures", new Property("textures", Base64Coder.encodeString(String.format(JSON_SKIN, args.toArray(new Object[0])))));
        return profile;
    }
}
