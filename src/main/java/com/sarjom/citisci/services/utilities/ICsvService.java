package com.sarjom.citisci.services.utilities;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface ICsvService {
    List<Map<String, String>> convertCsvToListOfMap(File file) throws Exception;
}
