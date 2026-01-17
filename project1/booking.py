class User:
    def __init__(self, user_id, name):
        self.id = user_id 
        self.name = name

class Seat:
    FREE = "свободно" 
    RESERVED = "забронировано" 
    SOLD = "продано"
    
    def __init__(self, seat_id):
        self.id = seat_id
        self.status = self.FREE
        self.user = None
    
    def __str__(self):
        user_name = self.user.name if self.user else "никто"
        return f"место {self.id}: {self.status} ({user_name})"

class EventSession:
    def __init__(self, session_id, time):
        self.id = session_id 
        self.time = time 
        self.seats = {}
        
        for i in range(1, 7):
            seat_id = f"A{i}"
            self.seats[seat_id] = Seat(seat_id)
    
    def show_seats(self):
        print(f"\nсеанс: {self.id}, время: {self.time}")
        for seat in self.seats.values():
            print(f"  {seat}")
    
    def get_seat(self, seat_id):
        return self.seats.get(seat_id)

class BookingSystem: 
    def __init__(self):
        self.history = []
    
    def reserve_seat(self, session, seat_id, user):
        seat = session.get_seat(seat_id)
        if seat and seat.status == Seat.FREE:
            old_state = (seat.status, seat.user)
            self.history.append(("reserve", seat_id, old_state))
            
            seat.status = Seat.RESERVED
            seat.user = user
            print(f"{user.name} забронировал место {seat_id}")
            return True
        else:
            print(f"не получилось забронировать {seat_id}")
            return False
    
    def cancel_reservation(self, session, seat_id, user):
        seat = session.get_seat(seat_id)
        if seat and seat.status == Seat.RESERVED and seat.user and seat.user.id == user.id:
            old_state = (seat.status, seat.user)
            self.history.append(("cancel", seat_id, old_state))
            
            seat.status = Seat.FREE
            seat.user = None
            print(f"{user.name} отменил бронь {seat_id}")
            return True
        else:
            print(f"не получилось отменить бронь {seat_id}")
            return False
    
    def buy_ticket(self, session, seat_id, user):
        seat = session.get_seat(seat_id)
        if seat and seat.status == Seat.RESERVED and seat.user and seat.user.id == user.id:
            old_state = (seat.status, seat.user)
            self.history.append(("buy", seat_id, old_state))
            
            seat.status = Seat.SOLD
            print(f"{user.name} купил билет на {seat_id}")
            return True
        else:
            print(f"не получилось купить билет на {seat_id}")
            return False
    
    def change_seat(self, session, old_seat_id, new_seat_id, user):
        old_seat = session.get_seat(old_seat_id)
        new_seat = session.get_seat(new_seat_id)
        
        if (old_seat and old_seat.status == Seat.RESERVED and 
            old_seat.user and old_seat.user.id == user.id and
            new_seat and new_seat.status == Seat.FREE):
            
            old_state = (old_seat.status, old_seat.user, new_seat.status, new_seat.user)
            self.history.append(("change", old_seat_id, new_seat_id, old_state))
            
            old_seat.status = Seat.FREE
            old_seat.user = None
            
            new_seat.status = Seat.RESERVED
            new_seat.user = user
            
            print(f"{user.name} пересел с {old_seat_id} на {new_seat_id}")
            return True
        else:
            print(f"не получилось сменить место")
            return False
    
    def undo_last(self, session):
        if not self.history:
            print("нечего отменять")
            return
        
        last_action = self.history.pop()
        
        if last_action[0] == "change":
            action_type, old_seat_id, new_seat_id, old_state = last_action
            old_seat = session.get_seat(old_seat_id)
            new_seat = session.get_seat(new_seat_id)
            
            if old_seat and new_seat:
                old_status, old_user, new_status, new_user = old_state
                old_seat.status = old_status
                old_seat.user = old_user
                new_seat.status = new_status
                new_seat.user = new_user
                print(f"отмена: смена места")
        else:
            action_type, seat_id, old_state = last_action
            seat = session.get_seat(seat_id)
            
            if seat:
                old_status, old_user = old_state
                seat.status = old_status
                seat.user = old_user
                print(f"отмена: {action_type} для места {seat_id}")

def main(): 
    print("программа бронирования билетов") 
    print("=" * 30)
    
    user1 = User("1", "иван")
    user2 = User("2", "мария")
    
    session = EventSession("концерт", "19:00")
    system = BookingSystem()
    
    print("\nначальное состояние:")
    session.show_seats()
    
    print("\n--- тест 1: бронирование ---")
    system.reserve_seat(session, "A1", user1)
    system.reserve_seat(session, "A2", user2)
    session.show_seats()
    
    print("\n--- тест 2: покупка билета ---")
    system.buy_ticket(session, "A1", user1)
    session.show_seats()
    
    print("\n--- тест 3: отмена покупки ---")
    system.undo_last(session)
    session.show_seats()
    
    print("\n--- тест 4: смена места ---")
    system.change_seat(session, "A2", "A3", user2)
    session.show_seats()
    
    print("\n--- тест 5: отмена брони ---")
    system.cancel_reservation(session, "A3", user2)
    session.show_seats()
    
    print("\n--- тест 6: ошибки ---")
    system.buy_ticket(session, "A5", user1)
    system.reserve_seat(session, "A1", user2)
    
    print("\nконечное состояние:")
    session.show_seats()

if __name__ == "__main__":
    main()

