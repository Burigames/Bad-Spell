package com.badspell.formula;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/** Server-owned disk store. Revision conflicts are rejected; rename is the only commit point. */
public final class FormulaStore {
    private final Path directory;
    private final FormulaValidator validator;
    public FormulaStore(Path directory,RuneRegistry registry) throws IOException {
        this.directory=directory.toAbsolutePath(); validator=new FormulaValidator(registry); Files.createDirectories(this.directory);
    }
    public synchronized void save(UUID actor,Formula formula,int expectedRevision) throws IOException {
        if(!formula.owner().equals(actor)) throw new SecurityException("Only the author may edit");
        var errors=validator.validate(formula);
        if(!errors.isEmpty()) throw new FormulaEngine.InvalidFormula(errors);
        Path target=path(formula.id());
        if(Files.exists(target)) {
            Formula old=load(formula.id());
            if(!old.owner().equals(actor)) throw new SecurityException("Owner mismatch");
            if(old.revision()!=expectedRevision || formula.revision()!=expectedRevision+1) throw new IllegalStateException("Revision conflict");
        } else if(expectedRevision!=0 || formula.revision()!=1) throw new IllegalStateException("Initial revision must be 1");
        byte[] data=FormulaCodec.encode(formula); Path tmp=Files.createTempFile(directory,"formula-",".tmp");
        try {
            Files.write(tmp,data);
            Files.move(tmp,target,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);
        } finally { Files.deleteIfExists(tmp); }
    }
    public synchronized Formula load(UUID id) throws IOException {
        Path file=path(id);
        if(Files.size(file)>FormulaCodec.MAX_BYTES) throw new IOException("Oversized formula; quarantine required");
        Formula f=FormulaCodec.decode(Files.readAllBytes(file));
        if(!f.id().equals(id)) throw new IOException("Formula identity mismatch");
        var errors=validator.validate(f); if(!errors.isEmpty()) throw new IOException("Stored graph invalid: "+errors);
        return f;
    }
    private Path path(UUID id) { return directory.resolve(id+".bspell"); }
}
