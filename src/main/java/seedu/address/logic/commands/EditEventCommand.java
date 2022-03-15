package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADD_FRIENDNAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATETIME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DESCRIPTION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_REMOVE_FRIENDNAME;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import seedu.address.commons.core.Messages;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.CollectionUtil;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.event.DateTime;
import seedu.address.model.event.Event;
import seedu.address.model.person.Description;
import seedu.address.model.person.Name;

/**
 * Edits the details of an existing person in the address book.
 */
public class EditEventCommand extends Command {

    public static final String COMMAND_WORD = "editevent";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits the details of the person identified "
            + "by the index number used in the displayed person list. "
            + "Existing values will be overwritten by the input values.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + "[" + PREFIX_NAME + "NEW_EVENT_NAME] "
            + "[" + PREFIX_DATETIME + "NEW_DATE_TIME] "
            + "[" + PREFIX_DESCRIPTION + "NEW_DESCRIPTION] "
            + "[" + PREFIX_ADD_FRIENDNAME + "ADD_FRIEND_NAME1, ADD_FRIEND_NAME2…] "
            + "[" + PREFIX_REMOVE_FRIENDNAME + "REMOVE_FRIEND_NAME1, REMOVE_FRIEND_NAME2…]...\n"
            + "Example: " + COMMAND_WORD + " 1 "
            + PREFIX_NAME + "2nd Birthday "
            + PREFIX_DATETIME + "16-08-2021 1600";

    public static final String MESSAGE_EDIT_PERSON_SUCCESS = "Edited Event: %1$s";
    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided.";
    public static final String MESSAGE_DUPLICATE_EVENT = "This person already exists in the address book.";

    private final Index index;
    private final EditEventDescriptor editEventDescriptor;

    /**
     * @param index of the person in the filtered person list to edit
     * @param editEventDescriptor details to edit the person with
     */
    public EditEventCommand(Index index, EditEventDescriptor editEventDescriptor) {
        requireNonNull(index);
        requireNonNull(editEventDescriptor);

        this.index = index;
        this.editEventDescriptor = new EditEventDescriptor(editEventDescriptor);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Event> lastShownList = model.getEventsList();

        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Event eventToEdit = lastShownList.get(index.getZeroBased());
        Event editedEvent = createEditedEvent(eventToEdit, editEventDescriptor);

        if (!eventToEdit.isSameEvent(editedEvent) && model.hasEvent(editedEvent)) {
            throw new CommandException(MESSAGE_DUPLICATE_EVENT);
        }

        model.setEvent(eventToEdit, editedEvent);
        return new CommandResult(String.format(MESSAGE_EDIT_PERSON_SUCCESS, editedEvent), false, false, true);
    }

    /**
     * Creates and returns a {@code Person} with the details of {@code personToEdit}
     * edited with {@code editPersonDescriptor}.
     */
    private static Event createEditedEvent(Event eventToEdit, EditEventDescriptor editEventDescriptor) {
        assert eventToEdit != null;

        Name updatedName = editEventDescriptor.getName().orElse(eventToEdit.getName());
        DateTime updatedDateTime = editEventDescriptor.getDateTime().orElse(eventToEdit.getDateTime());
        Description updatedDescription = editEventDescriptor.getDescription().orElse(eventToEdit.getDescription());
        Set<Name> addFriendNames = editEventDescriptor.getAddFriendNames().orElse(null);
        Set<Name> removeFriendNames = editEventDescriptor.getRemoveFriendNames().orElse(null);
        Set<Name> currentFriendName = eventToEdit.getFriendNames();
        Set<Name> updatedFriendNames = new HashSet<>(currentFriendName);
        if (addFriendNames != null) {
            updatedFriendNames.addAll(addFriendNames);
        }
        if (removeFriendNames != null) {
            updatedFriendNames.removeAll(removeFriendNames);
        }

        return new Event(updatedName, updatedDateTime, updatedDescription, updatedFriendNames);
    }

    @Override
    public boolean equals(Object other) {
        // short circuit if same object
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof EditEventCommand)) {
            return false;
        }

        // state check
        EditEventCommand e = (EditEventCommand) other;
        return index.equals(e.index)
                && editEventDescriptor.equals(e.editEventDescriptor);
    }

    /**
     * Stores the details to edit the person with. Each non-empty field value will replace the
     * corresponding field value of the person.
     */
    public static class EditEventDescriptor {
        private Name name;
        private DateTime dateTime;
        private Description description;
        private Set<Name> addFriendNames;
        private Set<Name> removeFriendNames;


        public EditEventDescriptor() {}

        /**
         * Copy constructor.
         * A defensive copy of {@code tags} is used internally.
         */
        public EditEventDescriptor(EditEventDescriptor toCopy) {
            setName(toCopy.name);
            setDateTime(toCopy.dateTime);
            setDescription(toCopy.description);
            setAddFriendNames(toCopy.addFriendNames);
            setRemoveFriendNames(toCopy.removeFriendNames);

        }

        /**
         * Returns true if at least one field is edited.
         */
        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyNonNull(name, dateTime, description, addFriendNames, removeFriendNames);
        }

        public void setName(Name name) {
            this.name = name;
        }

        public Optional<Name> getName() {
            return Optional.ofNullable(name);
        }

        public void setDateTime(DateTime dateTime) {
            this.dateTime = dateTime;
        }

        public Optional<DateTime> getDateTime() {
            return Optional.ofNullable(dateTime);
        }

        public void setDescription(Description description) {
            this.description = description;
        }

        public Optional<Description> getDescription() {
            return Optional.ofNullable(description);
        }

        /**
         * Sets {@code friendsNames} to this object's {@code friendsNames}.
         * A defensive copy of {@code friendsNames} is used internally.
         */
        public void setAddFriendNames(Set<Name> addFriendsNames) {
            this.addFriendNames = (addFriendsNames != null) ? new HashSet<>(addFriendsNames) : null;
        }

        /**
         * Returns an unmodifiable tag set, which throws {@code UnsupportedOperationException}
         * if modification is attempted.
         * Returns {@code Optional#empty()} if {@code tags} is null.
         */
        public Optional<Set<Name>> getAddFriendNames() {
            return (addFriendNames != null) ? Optional.of(Collections.unmodifiableSet(addFriendNames)) : Optional.empty();
        }

        /**
         * Sets {@code friendsNames} to this object's {@code friendsNames}.
         * A defensive copy of {@code friendsNames} is used internally.
         */
        public void setRemoveFriendNames(Set<Name> removeFriendNames) {
            this.removeFriendNames = (removeFriendNames != null) ? new HashSet<>(removeFriendNames) : null;
        }

        /**
         * Returns an unmodifiable tag set, which throws {@code UnsupportedOperationException}
         * if modification is attempted.
         * Returns {@code Optional#empty()} if {@code tags} is null.
         */
        public Optional<Set<Name>> getRemoveFriendNames() {
            return (removeFriendNames != null) ? Optional.of(Collections.unmodifiableSet(removeFriendNames)) : Optional.empty();
        }

        @Override
        public boolean equals(Object other) {
            // short circuit if same object
            if (other == this) {
                return true;
            }

            // instanceof handles nulls
            if (!(other instanceof EditEventDescriptor)) {
                return false;
            }

            // state check
            EditEventDescriptor e = (EditEventDescriptor) other;

            return getName().equals(e.getName())
                    && getDateTime().equals(e.getDateTime())
                    && getDescription().equals(e.getDescription())
                    && getAddFriendNames().equals(e.getAddFriendNames())
                    && getRemoveFriendNames().equals(e.getRemoveFriendNames());

        }
    }
}