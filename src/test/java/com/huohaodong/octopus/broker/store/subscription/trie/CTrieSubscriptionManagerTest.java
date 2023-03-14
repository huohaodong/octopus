package com.huohaodong.octopus.broker.store.subscription.trie;

import com.huohaodong.octopus.broker.store.subscription.impl.CTrieSubscriptionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static com.huohaodong.octopus.broker.store.subscription.trie.CTrieTest.clientSubOnTopic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CTrieSubscriptionManagerTest {
    private CTrieSubscriptionManager sut;

    @BeforeEach
    public void setUp() {
        sut = new CTrieSubscriptionManager();
    }

    @Test
    public void testMatchSimple() {
        Subscription slashSub = clientSubOnTopic("TempSensor1", "/");
        sut.subscribe(slashSub);
        assertThat(sut.match("finance")).isEmpty();

        Subscription slashFinanceSub = clientSubOnTopic("TempSensor1", "/finance");
        sut.subscribe(slashFinanceSub);
        assertThat(sut.match("finance")).isEmpty();

        assertThat(sut.match("/finance")).contains(slashFinanceSub);
        assertThat(sut.match("/")).contains(slashSub);
    }

    @Test
    public void testMatchSimpleMulti() {
        Subscription anySub = clientSubOnTopic("TempSensor1", "#");
        sut.subscribe(anySub);
        assertThat(sut.match("finance")).contains(anySub);

        Subscription financeAnySub = clientSubOnTopic("TempSensor1", "finance/#");
        sut.subscribe(financeAnySub);
        assertThat(sut.match("finance")).containsExactlyInAnyOrder(financeAnySub, anySub);
    }

    @Test
    public void testMatchingDeepMulti_one_layer() {
        Subscription anySub = clientSubOnTopic("AllSensor1", "#");
        Subscription financeAnySub = clientSubOnTopic("FinanceSensor", "finance/#");
        sut.subscribe(anySub);
        sut.subscribe(financeAnySub);

        // Verify
        assertThat(sut.match("finance/stock"))
                .containsExactlyInAnyOrder(financeAnySub, anySub);
        assertThat(sut.match("finance/stock/ibm"))
                .containsExactlyInAnyOrder(financeAnySub, anySub);
//        System.out.println(sut.dumpTree());
    }

    @Test
    public void testMatchingDeepMulti_two_layer() {
        Subscription financeAnySub = clientSubOnTopic("FinanceSensor", "finance/stock/#");
        sut.subscribe(financeAnySub);

        // Verify
        assertThat(sut.match("finance/stock/ibm")).containsExactly(financeAnySub);
    }

    @Test
    public void testMatchSimpleSingle() {
        Subscription anySub = clientSubOnTopic("AnySensor", "+");
        sut.subscribe(anySub);
        assertThat(sut.match("finance")).containsExactly(anySub);

        Subscription financeOne = clientSubOnTopic("AnySensor", "finance/+");
        sut.subscribe(financeOne);
        assertThat(sut.match("finance/stock")).containsExactly(financeOne);
    }

    @Test
    public void testMatchManySingle() {
        Subscription manySub = clientSubOnTopic("AnySensor", "+/+");
        sut.subscribe(manySub);

        // verify
        assertThat(sut.match("/finance")).contains(manySub);
    }

    @Test
    public void testMatchSlashSingle() {
        Subscription slashPlusSub = clientSubOnTopic("AnySensor", "/+");
        sut.subscribe(slashPlusSub);
        Subscription anySub = clientSubOnTopic("AnySensor", "+");
        sut.subscribe(anySub);

        // Verify
        assertThat(sut.match("/finance")).containsOnly(slashPlusSub);
        assertThat(sut.match("/finance")).doesNotContain(anySub);
    }

    @Test
    public void testMatchManyDeepSingle() {
        Subscription slashPlusSub = clientSubOnTopic("FinanceSensor1", "/finance/+/ibm");
        sut.subscribe(slashPlusSub);
        Subscription slashPlusDeepSub = clientSubOnTopic("FinanceSensor2", "/+/stock/+");
        sut.subscribe(slashPlusDeepSub);

        // Verify
        assertThat(sut.match("/finance/stock/ibm"))
                .containsExactlyInAnyOrder(slashPlusSub, slashPlusDeepSub);
    }

    @Test
    public void testMatchSimpleMulti_allTheTree() {
        Subscription sub = clientSubOnTopic("AnySensor1", "#");
        sut.subscribe(sub);

        assertThat(sut.match("finance")).isNotEmpty();
        assertThat(sut.match("finance/ibm")).isNotEmpty();
    }

    @Test
    public void rogerLightTopicMatches() {
        assertMatch("foo/bar", "foo/bar");
        assertMatch("foo/bar", "foo/bar");
        assertMatch("foo/+", "foo/bar");
        assertMatch("foo/+/baz", "foo/bar/baz");
        assertMatch("foo/+/#", "foo/bar/baz");
        assertMatch("#", "foo/bar/baz");

        assertNotMatch("foo/bar", "foo");
        assertNotMatch("foo/+", "foo/bar/baz");
        assertNotMatch("foo/+/baz", "foo/bar/bar");
        assertNotMatch("foo/+/#", "fo2/bar/baz");

        assertMatch("#", "/foo/bar");
        assertMatch("/#", "/foo/bar");
        assertNotMatch("/#", "foo/bar");

        assertMatch("foo//bar", "foo//bar");
        assertMatch("foo//+", "foo//bar");
        assertMatch("foo/+/+/baz", "foo///baz");
        assertMatch("foo/bar/+", "foo/bar/");
    }

    private void assertMatch(String s, String t) {
        sut = new CTrieSubscriptionManager();

        Subscription sub = clientSubOnTopic("AnySensor1", s);
        sut.subscribe(sub);

        assertThat(sut.match(t)).isNotEmpty();
    }

    private void assertNotMatch(String subscription, String topic) {
        sut = new CTrieSubscriptionManager();

        Subscription sub = clientSubOnTopic("AnySensor1", subscription);
        sut.subscribe(sub);

        assertThat(sut.match(topic)).isEmpty();
    }

    @Test
    public void testOverlappingSubscriptions() throws InterruptedException {
        Subscription genericSub = new Subscription("Sensor1", "a/+", null);
        sut.subscribe(genericSub);

        Subscription specificSub = new Subscription("Sensor1", "a/b", null);
        sut.subscribe(specificSub);

        //Exercise
        final Set<Subscription> matchingForSpecific = sut.match("a/b");

        // Verify
        assertThat(matchingForSpecific.size()).isEqualTo(2);
    }

    @Test
    public void removeSubscription_withDifferentClients_subscribedSameTopic() {
        Subscription slashSub = clientSubOnTopic("Sensor1", "/topic");
        sut.subscribe(slashSub);
        Subscription slashSub2 = clientSubOnTopic("Sensor2", "/topic");
        sut.subscribe(slashSub2);

        // Exercise
        sut.unSubscribe("/topic", slashSub2.getClientId());

        // Verify
        Subscription remainedSubscription = sut.match("/topic").iterator().next();
        assertThat(remainedSubscription.getClientId()).isEqualTo(slashSub.getClientId());
        assertEquals(slashSub.getClientId(), remainedSubscription.getClientId());
    }

    @Test
    public void removeSubscription_sameClients_subscribedSameTopic() {
        Subscription slashSub = clientSubOnTopic("Sensor1", "/topic");
        sut.subscribe(slashSub);

        // Exercise
        sut.unSubscribe("/topic", slashSub.getClientId());

        // Verify
        final Set<Subscription> matchingSubscriptions = sut.match("/topic");
        assertThat(matchingSubscriptions).isEmpty();
    }

    /*
     * Test for Issue #49
     */
    @Test
    public void duplicatedSubscriptionsWithDifferentQos() {
        Subscription client2Sub = new Subscription("client2", "client/test/b", null);
        this.sut.subscribe(client2Sub);
        Subscription client1SubQoS0 = new Subscription("client1", "client/test/b", null);
        this.sut.subscribe(client1SubQoS0);

        Subscription client1SubQoS2 = new Subscription("client1", "client/test/b", null);
        this.sut.subscribe(client1SubQoS2);

        // Verify
        Set<Subscription> subscriptions = this.sut.match("client/test/b");
        assertThat(subscriptions).contains(client1SubQoS2);
        assertThat(subscriptions).contains(client2Sub);

        final Optional<Subscription> matchingClient1Sub = subscriptions
                .stream()
                .filter(s -> s.equals(client1SubQoS0))
                .findFirst();
        assertTrue(matchingClient1Sub.isPresent());
        Subscription client1Sub = matchingClient1Sub.get();
    }

}