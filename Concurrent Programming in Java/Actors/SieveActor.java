package edu.coursera.concurrent;

import edu.rice.pcdp.Actor;

import static edu.rice.pcdp.PCDP.finish;

/**
 * An actor-based implementation of the Sieve of Eratosthenes.
 *
 * TODO Fill in the empty SieveActorActor actor class below and use it from
 * countPrimes to determin the number of primes <= limit.
 */
public final class SieveActor extends Sieve {
    /**
     * {@inheritDoc}
     *
     * TODO Use the SieveActorActor class to calculate the number of primes <=
     * limit in parallel. You might consider how you can model the Sieve of
     * Eratosthenes as a pipeline of actors, each corresponding to a single
     * prime number.
     */
    @Override
    public int countPrimes(final int limit) {
        SieveActorActor sieveActor = new SieveActorActor(2);
        finish(() -> {
            for(int i = 3; i <= limit; i+=2){
                sieveActor.send(i);
            }
            sieveActor.send(0);
        });

        int numPrimes = 0;
        SieveActorActor curActor = sieveActor;
        while(curActor != null){
            numPrimes += curActor.numLocalPrimes();
            curActor = curActor.nextActor();
        }
        return numPrimes;
    }

    /**
     * An actor class that helps implement the Sieve of Eratosthenes in
     * parallel.
     */
    public static final class SieveActorActor extends Actor {

        static final int MAX_LOCAL_PRIMES = 1000;
        int localPrimes[] = new int[MAX_LOCAL_PRIMES];
        int numLocalPrimes = 0;
        SieveActorActor nextActor = null;

        SieveActorActor(final int localPrime){
            this.localPrimes = new int[MAX_LOCAL_PRIMES];
            this.localPrimes[0] = localPrime;
            this.numLocalPrimes = 1;
            this.nextActor = null;
        }
        

        public SieveActorActor nextActor() { return this.nextActor; }

        public boolean isThereANextActor() { return this.nextActor != null; }

        public int numLocalPrimes() { return this.numLocalPrimes; }

        /**
         * Process a single message sent to this actor.
         *
         * TODO complete this method.
         *
         * @param msg Received message
         */
        @Override
        public void process(final Object msg) {
            final int candidate = (Integer)msg;
            if(candidate <= 0) {
                if(isThereANextActor()) {
                    nextActor.send(msg);
                }
                return;
                //exit()
            } else {

                if(isLocallyPrime(candidate)) {
                    if(numLocalPrimes < MAX_LOCAL_PRIMES) {
                        localPrimes[numLocalPrimes] = candidate;
                        numLocalPrimes++;
                    } else {
                        if (nextActor == null) { nextActor = new SieveActorActor(candidate); }
                        else { nextActor.send(msg); }
                    }

                }
            }
        }

        private boolean isLocallyPrime(int candidate) {
            //int sqrtnum = (int)Math.sqrt(numLocalPrimes);
            for(int i = 0; i < numLocalPrimes; ++i){
                if(candidate % localPrimes[i] == 0) {
                    return false;
                }
            }
            return true;
        } 

    } 
}
