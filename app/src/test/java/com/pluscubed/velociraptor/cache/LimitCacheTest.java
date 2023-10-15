package com.pluscubed.velociraptor.cache;

import android.os.Build;

import com.pluscubed.velociraptor.BuildConfig;
import com.pluscubed.velociraptor.api.Coord;
import com.pluscubed.velociraptor.api.LimitResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

//import rx.observers.TestSubscriber;

import static org.junit.Assert.assertThat;

//@RunWith(RobolectricTestRunner.class)
//@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.O_MR1)
//public class LimitCacheTest {
//
//    CacheLimitProvider cacheLimitProvider;
//    //private File cache;
//    private LimitResponse response;
//
//    @Before
//    public void setUp() throws Exception {
//        //cache = new File("cache.json");
//        cacheLimitProvider = new CacheLimitProvider(RuntimeEnvironment.application);
//
//        List<Coord> coords = new ArrayList<>();
//        //Alta/Charleston - Huff/Charleston
//        coords.add(new Coord(37.420915, -122.085359));
//        coords.add(new Coord(37.420774, -122.082945));
//
//        response = LimitResponse.Companion.builder()
//                .setCoords(coords)
//                .setTimestamp(System.currentTimeMillis())
//                .setRoadName("Charleston Road")
//                .setSpeedLimit(35)
//                .build();
//    }
//
//    @Test
//    public void put_singleResponseSuccess() throws Exception {
//        cacheLimitProvider.put(response);
//
//        //assertThat(cacheLimitProvider.responses.size(), is(1));
//
//        /*ObjectMapper mapper = new ObjectMapper();
//        List<ApiResponse> responses = mapper.readValue(cache, new TypeReference<List<ApiResponse>>() {
//        });
//        assertThat(responses.get(0), is(response));*/
//    }
//
//    @Test
//    public void get_coordOnRoad() throws Exception {
//        cacheLimitProvider.put(response);
//
//        TestSubscriber<LimitResponse> testSubscriber = new TestSubscriber<>();
//        cacheLimitProvider.get(null, new Coord(37.420902, -122.084073)).subscribe(testSubscriber);
//
//        testSubscriber.assertValueCount(1);
//        LimitResponse received = testSubscriber.getOnNextEvents().get(0);
//        assertThat(received.speedLimit(), is(response.speedLimit()));
//        assertThat(received.roadName(), is(response.roadName()));
//        assertThat(received.timestamp(), is(response.timestamp()));
//    }
//
//    @Test
//    public void get_coordNotOnRoad() throws Exception {
//        cacheLimitProvider.put(response);
//
//        TestSubscriber<LimitResponse> testSubscriber = new TestSubscriber<>();
//        cacheLimitProvider.get(null, new Coord(37.419188, -122.085396)).subscribe(testSubscriber);
//
//        testSubscriber.assertNoValues();
//        testSubscriber.assertCompleted();
//    }
//
//    @Test
//    public void get_oneResponseValidWithPreviousName() throws Exception {
//        cacheLimitProvider.put(response);
//
//        List<Coord> coords = new ArrayList<>();
//        //Same as normal
//        coords.add(new Coord(37.420915, -122.085359));
//        coords.add(new Coord(37.420774, -122.082945));
//
//        LimitResponse otherResponse = LimitResponse.Companion.builder()
//                .setTimestamp(System.currentTimeMillis())
//                .setRoadName("Not Charleston Road")
//                .setSpeedLimit(15)
//                .build();
//
//        cacheLimitProvider.put(otherResponse);
//
//        TestSubscriber<LimitResponse> testSubscriber = new TestSubscriber<>();
//        cacheLimitProvider.get("Charleston Road", new Coord(37.420902, -122.084073))
//                .subscribe(testSubscriber);
//
//        testSubscriber.assertValueCount(1);
//        LimitResponse received = testSubscriber.getOnNextEvents().get(0);
//        assertThat(received.speedLimit(), is(response.speedLimit()));
//        assertThat(received.roadName(), is(response.roadName()));
//        assertThat(received.timestamp(), is(response.timestamp()));
//    }
//
//    @Test
//    public void get_oldCache() throws Exception {
//        //One second older than 1 week
//        response = response.toBuilder()
//                .setTimestamp(System.currentTimeMillis() - (6_048_0000_0000L + 1000L))
//                .build();
//        cacheLimitProvider.put(response);
//
//        TestSubscriber<LimitResponse> testSubscriber = new TestSubscriber<>();
//        cacheLimitProvider.get("Charleston Road", new Coord(37.420902, -122.084073))
//                .subscribe(testSubscriber);
//
//        testSubscriber.assertNoValues();
//        testSubscriber.assertCompleted();
//    }
//
//
//    @After
//    public void tearDown() throws Exception {
//        //cacheLimitProvider.responses.clear();
//        //cache.delete();
//    }
//}