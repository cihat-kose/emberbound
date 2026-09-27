package dev.emberbound.persistence;

import dev.emberbound.domain.Expedition;
import java.io.IOException;

/** Persistence boundary, injectable without a filesystem in application tests. */
public interface SaveStore {
    Expedition load() throws IOException;

    void save(Expedition expedition) throws IOException;
}
