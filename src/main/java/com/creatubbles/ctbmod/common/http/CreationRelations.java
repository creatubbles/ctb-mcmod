package com.creatubbles.ctbmod.common.http;

import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ObjectUtils;

import jersey.repackaged.com.google.common.collect.Maps;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Delegate;

import com.creatubbles.api.core.Creation;
import com.creatubbles.api.request.auth.OAuthAccessTokenRequest;
import com.creatubbles.api.request.creation.GetCreationRequest;
import com.creatubbles.api.request.user.UserProfileRequest;
import com.creatubbles.api.response.auth.OAuthAccessTokenResponse;
import com.creatubbles.api.response.creation.GetCreationResponse;
import com.creatubbles.api.response.relationships.Relationships;
import com.creatubbles.api.response.user.UserProfileResponse;
import com.creatubbles.ctbmod.CTBMod;
import com.creatubbles.ctbmod.common.util.ConcurrentUtil;
import com.google.common.util.concurrent.ListenableFuture;

@Getter
public class CreationRelations extends Creation {
	
	@Delegate
	private final Creation creation;
	
	@Nullable
	@Setter
	private Relationships relationships;
	
	@Getter
	@Nullable
	private transient ListenableFuture<?> completionCallback;
	
	public CreationRelations(@NonNull Creation c, @Nullable Relationships r) {
		creation = c;
		relationships = r;
	}
	
	public CreationRelations(GetCreationResponse resp) {
	    this(resp.getCreation(), resp.getRelationships());
	}
	
    private static final Map<String, CreationRelations> completing = Maps.newConcurrentMap();

    /**
     * Adds relationships to a creation, used for completing old NBT data.
     * 
     * The returned object will have a null relationship object until the async task completes.
     * 
     * @param c
     *            The creation to complete
     * @return A soon-to-be completed {@link CreationRelations} object.
     */
    public static CreationRelations complete(final Creation c) {
        if (completing.containsKey(c.getId())) {
            return completing.get(c.getId());
        }
        final CreationRelations ret = new CreationRelations(c, null);
        ret.completionCallback = ConcurrentUtil.execute(new Runnable() { { completing.put(ret.getCreation().getId(), ret); }

            @Override
            public void run() {
                String accessToken = CTBMod.cache.getOAuth() == null ? null : CTBMod.cache.getOAuth().getAccessToken();
                if (accessToken == null) {
                    OAuthAccessTokenRequest authReq = new OAuthAccessTokenRequest(OAuthUtil.CLIENT_ID, OAuthUtil.CLIENT_SECRET);
                    OAuthAccessTokenResponse authResp = authReq.execute().getResponse();
                    accessToken = authResp.getAccessToken();
                }

                GetCreationRequest req = new GetCreationRequest(c.getId(), accessToken);
                GetCreationResponse resp = req.execute().getResponse();
                ret.setRelationships(resp.getRelationships());
            }
        });
        return ret;
    }
}
