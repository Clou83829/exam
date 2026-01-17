import java.util.*;

public class EventBookingSystem{

static class User {
    String id;
    String name;
    
    User(String id, String name) {
        this.id = id;
        this.name = name;
    }
}

static class Seat {
    String id;
    String status;
    User user;
    
    Seat(String id) {
        this.id = id;
        this.status = "FREE";
        this.user = null;
    }
}

static class EventSession {
    String id;
    String time;
    Map<String, Seat> seats = new HashMap<>();
    
    EventSession(String id, String time) {
        this.id = id;
        this.time = time;
        for (int i = 1; i <= 6; i++) {
            String seatId = "A" + i;
            seats.put(seatId, new Seat(seatId));
        }
    }
    
    void showSeats() {
        System.out.println("\nсеанс: " + id + " время: " + time);
        System.out.println("места:");
        for (Seat seat : seats.values()) {
            String who = (seat.user != null) ? seat.user.name : "никто";
            System.out.println("  место " + seat.id + ": " + seat.status + " (" + who + ")");
        }
    }
}

interface BookingCommand {
    void execute(EventSession session, String seatId, User user);
    void undo(EventSession session, String seatId, User user);
}

static class ReserveCommand implements BookingCommand {
    String oldStatus;
    User oldUser;
    
    public void execute(EventSession session, String seatId, User user) {
        Seat seat = session.seats.get(seatId);
        if (seat != null && seat.status.equals("FREE")) {
            oldStatus = seat.status;
            oldUser = seat.user;
            
            seat.status = "RESERVED";
            seat.user = user;
            System.out.println("место " + seatId + " забронировано для " + user.name);
        } else {
            System.out.println("не получилось забронировать место " + seatId + " (уже занято)");
        }
    }
    
    public void undo(EventSession session, String seatId, User user) {
        Seat seat = session.seats.get(seatId);
        if (seat != null) {
            seat.status = oldStatus;
            seat.user = oldUser;
            System.out.println("отмена бронирования места " + seatId);
        }
    }
}

static class BuyTicketCommand implements BookingCommand {
    String oldStatus;
    
    public void execute(EventSession session, String seatId, User user) {
        Seat seat = session.seats.get(seatId);
        if (seat != null && seat.status.equals("RESERVED") 
            && seat.user.id.equals(user.id)) {
            
            oldStatus = seat.status;
            seat.status = "SOLD";
            System.out.println(user.name + " купил билет на место " + seatId);
        } else {
            System.out.println("нельзя купить билет на место " + seatId);
        }
    }
    
    public void undo(EventSession session, String seatId, User user) {
        Seat seat = session.seats.get(seatId);
        if (seat != null) {
            seat.status = oldStatus;
            System.out.println("отмена покупки билета на место " + seatId);
        }
    }
}

static class CancelCommand implements BookingCommand {
    String oldStatus;
    User oldUser;
    
    public void execute(EventSession session, String seatId, User user) {
        Seat seat = session.seats.get(seatId);
        if (seat != null && seat.status.equals("RESERVED") 
            && seat.user.id.equals(user.id)) {
            
            oldStatus = seat.status;
            oldUser = seat.user;
            
            seat.status = "FREE";
            seat.user = null;
            System.out.println(user.name + " отменил бронь места " + seatId);
        } else {
            System.out.println("нельзя отменить бронь места " + seatId);
        }
    }
    
    public void undo(EventSession session, String seatId, User user) {
        Seat seat = session.seats.get(seatId);
        if (seat != null) {
            seat.status = oldStatus;
            seat.user = oldUser;
        }
    }
}

static class ChangeSeatCommand implements BookingCommand {
    String oldSeatId;
    String oldStatus;
    User oldUser;
    
    public void execute(EventSession session, String newSeatId, User user) {
        String currentSeatId = null;
        for (Map.Entry<String, Seat> entry : session.seats.entrySet()) {
            if (entry.getValue().user != null && 
                entry.getValue().user.id.equals(user.id) &&
                entry.getValue().status.equals("RESERVED")) {
                currentSeatId = entry.getKey();
                break;
            }
        }
        
        if (currentSeatId != null) {
            Seat currentSeat = session.seats.get(currentSeatId);
            Seat newSeat = session.seats.get(newSeatId);
            
            if (newSeat != null && newSeat.status.equals("FREE")) {
                oldSeatId = currentSeatId;
                oldStatus = currentSeat.status;
                oldUser = currentSeat.user;
                
                currentSeat.status = "FREE";
                currentSeat.user = null;
                
                newSeat.status = "RESERVED";
                newSeat.user = user;
                
                System.out.println(user.name + " пересел с места " + currentSeatId + " на место " + newSeatId);
            } else {
                System.out.println("не удалось сменить место");
            }
        } else {
            System.out.println("у пользователя нет забронированных мест");
        }
    }
    
    public void undo(EventSession session, String seatId, User user) {
        if (oldSeatId != null) {
            Seat oldSeat = session.seats.get(oldSeatId);
            Seat newSeat = session.seats.get(seatId);
            
            oldSeat.status = oldStatus;
            oldSeat.user = oldUser;
            
            newSeat.status = "FREE";
            newSeat.user = null;
            System.out.println("отмена смены места");
        }
    }
}

static class CommandHandler {
    Stack<BookingCommand> history = new Stack<>();
    
    void execute(BookingCommand command, EventSession session, String seatId, User user) {
        command.execute(session, seatId, user);
        history.push(command);
    }
    
    void undo(EventSession session, String seatId, User user) {
        if (!history.isEmpty()) {
            BookingCommand lastCommand = history.pop();
            lastCommand.undo(session, seatId, user);
        } else {
            System.out.println("нечего отменять");
        }
    }
}

public static void main(String[] args) {
    System.out.println("начало работы программы бронирования");
    
    User user1 = new User("1", "иван");
    User user2 = new User("2", "мария");
    
    EventSession session = new EventSession("концерт", "19:00");
    
    CommandHandler handler = new CommandHandler();
    
    System.out.println("\nначальное состояние зала:");
    session.showSeats();
    
    System.out.println("\n=== тест 1: бронирование мест ===");
    handler.execute(new ReserveCommand(), session, "A1", user1);
    handler.execute(new ReserveCommand(), session, "A2", user2);
    session.showSeats();
    
    System.out.println("\n=== тест 2: покупка билета ===");
    handler.execute(new BuyTicketCommand(), session, "A1", user1);
    session.showSeats();
    
    System.out.println("\n=== тест 3: отмена последней операции ===");
    handler.undo(session, "A1", user1);
    session.showSeats();
    
    System.out.println("\n=== тест 4: смена места ===");
    handler.execute(new ChangeSeatCommand(), session, "A3", user2);
    session.showSeats();
    
    System.out.println("\n=== тест 5: отмена брони ===");
    handler.execute(new CancelCommand(), session, "A3", user2);
    session.showSeats();
    
    System.out.println("\n=== тест 6: попытка ошибочных действий ===");
    handler.execute(new BuyTicketCommand(), session, "A5", user1);
    handler.execute(new ReserveCommand(), session, "A1", user2);
    
    System.out.println("\nконечное состояние зала:");
    session.showSeats();
    
    System.out.println("\nпрограмма завершена");
}
}