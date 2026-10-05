package org.softwarecave.springjpa.outbox.tools;


import lombok.extern.slf4j.Slf4j;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import org.softwarecave.springjpa.outbox.service.InvalidOutboxDataException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
public class AvroTools {

    public static <T extends SpecificRecord> byte[] convertToBytes(T record) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            SpecificDatumWriter<SpecificRecord> writer = new SpecificDatumWriter<>(record.getSchema());
            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(outputStream, null);

            writer.write(record, encoder);
            encoder.flush();

            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new InvalidOutboxDataException("Failed to convert Avro record into bytes", e);
        }
    }

    public static <T> T fromBytes(byte[] data, Class<T> avroClass) throws Exception {
        DatumReader<T> reader = new SpecificDatumReader<>(avroClass);
        BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(data, null);
        return reader.read(null, decoder);
    }
}
