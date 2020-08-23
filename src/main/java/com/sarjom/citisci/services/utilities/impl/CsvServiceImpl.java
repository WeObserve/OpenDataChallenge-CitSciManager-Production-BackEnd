package com.sarjom.citisci.services.utilities.impl;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.sarjom.citisci.services.utilities.ICsvService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CsvServiceImpl implements ICsvService {
    private static Logger logger = LoggerFactory.getLogger(CsvServiceImpl.class);

    @Override
    public List<Map<String, String>> convertCsvToListOfMap(File file) throws Exception {
        logger.info("Inside convertCsvToListOfMap");

        List<Map<String, String>> response = new ArrayList<>();

        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = CsvSchema.emptySchema().withHeader();

        MappingIterator<Map<String, String>> iterator = mapper.reader(Map.class).with(schema).readValues(file);

        while(iterator.hasNext()) {
            response.add(iterator.next());
        }

        return response;
    }
}
