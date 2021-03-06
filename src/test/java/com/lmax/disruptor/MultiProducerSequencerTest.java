/*
 * Copyright 2012 LMAX Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lmax.disruptor;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class MultiProducerSequencerTest
{
    private final Sequencer sequencer = new MultiProducerSequencer(16, new BlockingWaitStrategy());
    private final Sequence initialGatingSequence = new Sequence();
    private final Sequence dynamicallyAddedGatingSequence = new Sequence();

    @Before
    public void setUp()
    {
        sequencer.addGatingSequences(initialGatingSequence);
    }

    @Test
    public void shouldOnlyAllowMessagesToBeAvailableIfSpecificallyPublished() throws Exception
    {
        sequencer.publish(3);
        sequencer.publish(5);

        assertThat(sequencer.isAvailable(0), is(false));
        assertThat(sequencer.isAvailable(1), is(false));
        assertThat(sequencer.isAvailable(2), is(false));
        assertThat(sequencer.isAvailable(3), is(true));
        assertThat(sequencer.isAvailable(4), is(false));
        assertThat(sequencer.isAvailable(5), is(true));
        assertThat(sequencer.isAvailable(6), is(false));
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
