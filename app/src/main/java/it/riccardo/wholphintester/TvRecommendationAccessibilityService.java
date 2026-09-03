private String cleanTitle(String text) {

    String title = text.trim();

    /*
     * Google TV può aggiungere informazioni commerciali
     * dopo il titolo, ad esempio:
     *
     * "Il dio dell'amore, costo 11,99€"
     * "Film XYZ, richiede l'abbonamento a HBO Plus"
     */

    String lower = title.toLowerCase();

    String[] separators = {
            "richiede l'abbonamento",
            "richiede abbonamento",
            "disponibile con l'abbonamento",
            "disponibile con abbonamento",
            "guarda con l'abbonamento",
            "guarda con abbonamento",
            "acquista",
            "noleggia",
            "noleggio",
            "acquisto",
            "costo"
    };

    int cutIndex = -1;

    for (String separator : separators) {

        int index = lower.indexOf(separator);

        if (index >= 0 &&
                (cutIndex == -1 || index < cutIndex)) {

            cutIndex = index;
        }
    }

    if (cutIndex >= 0) {
        title = title.substring(0, cutIndex);
    }

    /*
     * Elimina la virgola rimasta alla fine:
     *
     * "Film XYZ,"
     */
    title = title.trim();

    while (title.endsWith(",")) {
        title = title.substring(
                0,
                title.length() - 1
        ).trim();
    }

    return title;
}
