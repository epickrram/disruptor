package com.lmax.disruptor;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class SingleProducerSequencerTest
{
    private final SingleProducerSequencer sequencer = new SingleProducerSequencer(16, new BlockingWaitStrategy());
    private final Sequence initialGatingSequence = new Sequence();
    private final Sequence dynamicallyAddedGatingSequence = new Sequence();

    @Before
    public void setUp()
    {
        sequencer.addGatingSequences(initialGatingSequence);
    }

    @Test
    public void shouldApplyGatingSequence() throws Exception
    {
        sequencer.publish(sequencer.next());
        sequencer.publish(sequencer.next());
        sequencer.publish(sequencer.next());
        sequencer.publish(sequencer.next());
        sequencer.publish(sequencer.next());

        initialGatingSequence.set(1L);

        assertThat(sequencer.getCursor(), is(4L));
        assertTrue(sequencer.hasAvailableCapacity(13));
        assertFalse(sequencer.hasAvailableCapacity(14));
    }

    @Test
    public void shouldRespectDynamicallyAddedGatingSequence() throws Exception
    {
        sequencer.publish(sequencer.next());
        sequencer.publish(sequencer.next());
        sequencer.publish(sequencer.next());
        sequencer.publish(sequencer.next());
        sequencer.publish(sequencer.next());

        initialGatingSequence.set(1L);

        assertThat(sequencer.getCursor(), is(4L));
        assertTrue(sequencer.hasAvailableCapacity(13));
        assertFalse(sequencer.hasAvailableCapacity(14));

        sequencer.addGatingSequences(dynamicallyAddedGatingSequence);

        dynamicallyAddedGatingSequence.set(0L);

        assertTrue(sequencer.hasAvailableCapacity(12));
        assertFalse(sequencer.hasAvailableCapacity(13));
    }

}