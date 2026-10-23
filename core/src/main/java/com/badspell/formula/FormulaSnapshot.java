package com.badspell.formula;

import java.io.IOException;
import java.security.*;
import java.util.HexFormat;

/** Content checksum detects corruption, not owner authentication (which belongs to the server). */
public final class FormulaSnapshot {
    private final Formula formula;
    private final String checksum;
    public FormulaSnapshot(Formula formula) {
        this.formula=formula;
        try { checksum=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(FormulaCodec.encode(formula))); }
        catch(IOException e) { throw new IllegalArgumentException("Cannot snapshot formula",e); }
        catch(NoSuchAlgorithmException e) { throw new AssertionError(e); }
    }
    public Formula formula() { return formula; }
    public String checksum() { return checksum; }
    public boolean verifies(String claimedChecksum) { return checksum.equals(claimedChecksum); }
}
