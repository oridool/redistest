package org.redistest;

import com.nimbusds.jose.crypto.bc.BouncyCastleProviderSingleton;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;

/**
 * BouncyCastle <b>FIPS</b> JCA provider singleton, intended to prevent memory leaks by
 * ensuring a single instance is loaded at all times. Application code that
 * needs a BouncyCastle JCA provider should use the {@link #getInstance()}
 * method to obtain an instance.
 * Inspired by {@link BouncyCastleProviderSingleton}
 */
public final class BouncyCastleFipsProviderSingleton {


    /**
     * The BouncyCastle FIPS provider, lazily instantiated.
     */
    private static BouncyCastleFipsProvider bouncyCastleProvider;


    /**
     * Prevents external instantiation.
     */
    private BouncyCastleFipsProviderSingleton() { }


    /**
     * Returns a BouncyCastle FIPS JCA provider instance.
     *
     * @return The BouncyCastle FIPS JCA provider instance.
     */
    public static BouncyCastleFipsProvider getInstance() {

        if (bouncyCastleProvider == null) {
            bouncyCastleProvider = new BouncyCastleFipsProvider();
        }
        return bouncyCastleProvider;
    }
}
