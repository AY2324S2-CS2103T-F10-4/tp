package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.storage.JsonSerializableAddressBook.ASSIGNMENTS_PROPERTY;
import static seedu.address.storage.JsonSerializableAddressBook.PERSONS_PROPERTY;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.json.CDL;
import org.json.JSONArray;
import org.json.JSONObject;

import seedu.address.commons.util.FileUtil;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/**
 * Export all the information of people and assignments in the entire address book as comma-separated values files.
 * The files are located in data directory.
 */
// Solution below inspired by https://chat.openai.com/c/cf700fda-7623-46fb-b506-7bf794bb6475
public class ExportCommand extends Command {

    public static final String COMMAND_WORD = "export";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Export the information of all the people and activities in the application to a CSV file.\n";

    public static final String MESSAGE_SUCCESS = "Data successfully exported to data directory.";
    public static final String MESSAGE_JSON_FILE_ABSENT =
            "Unable to locate [JAR file location]/data/addressbook.json file.";
    public static final String MESSAGE_CSV_IS_USED =
            "persons.csv or assignment.csv is being used by another application";

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Path addressBookFilePath = model.getAddressBookFilePath();

        if (addressBookFilePath == null || !Files.exists(addressBookFilePath)) {
            throw new CommandException(MESSAGE_JSON_FILE_ABSENT);
        }

        try {
            exportCsv(addressBookFilePath);
        } catch (IOException | AssertionError e) {
            throw new CommandException(MESSAGE_CSV_IS_USED);
        }

        return new CommandResult(String.format(MESSAGE_SUCCESS));
    }

    /**
     * Exports persons and assignment information into 2 separate comma-separated values file by taking in the
     * path to the address book.
     *
     * @param addressBookFilePath A path to the address book file.
     */
    public void exportCsv(Path addressBookFilePath) throws IOException {
        String jsonString = new String(Files.readAllBytes(addressBookFilePath));
        JSONObject jsonObject = new JSONObject(jsonString);

        Path addressBookDirectoryPath = addressBookFilePath.getParent();
        assert addressBookDirectoryPath != null;

        JSONArray jsonArrayPersons = jsonObject.getJSONArray(PERSONS_PROPERTY);
        JSONArray jsonArrayAssignments = jsonObject.getJSONArray(ASSIGNMENTS_PROPERTY);

        Path csvFilePathPersons = Path.of(addressBookDirectoryPath + "/" + PERSONS_PROPERTY + ".csv");
        Path csvFilePathAssignments = Path.of(addressBookDirectoryPath + "/" + ASSIGNMENTS_PROPERTY + ".csv");

        String csvStringPersons = CDL.toString(jsonArrayPersons);
        String csvStringAssignments = CDL.toString(jsonArrayAssignments);

        if (csvStringPersons == null) {
            csvStringPersons = "";
        }
        if (csvStringAssignments == null) {
            csvStringAssignments = "";
        }

        FileUtil.writeToFile(csvFilePathPersons, csvStringPersons);
        FileUtil.writeToFile(csvFilePathAssignments, csvStringAssignments);
    }
}
