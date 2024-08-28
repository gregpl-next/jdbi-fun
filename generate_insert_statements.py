import random
import datetime

num_records = 50000

first_names = [
    "John", "Jane", "Alex", "Emily", "Michael", "Sarah", "David", "Jessica", "Chris", "Amanda",
    "James", "Patricia", "Robert", "Linda", "William", "Elizabeth", "Joseph", "Barbara", "Thomas", "Jennifer",
    "Daniel", "Maria", "Paul", "Nancy", "Mark", "Lisa", "George", "Karen", "Steven", "Betty",
    "Andrew", "Margaret", "Kenneth", "Sandra", "Joshua", "Ashley", "Kevin", "Dorothy", "Brian", "Kimberly",
    "Edward", "Donna", "Ronald", "Carol", "Anthony", "Michelle", "Timothy", "Evelyn", "Jason", "Helen",
    "Matthew", "Laura", "Gary", "Rebecca", "Ryan", "Sharon", "Nicholas", "Cynthia", "Eric", "Kathleen",
    "Jacob", "Amy", "Stephen", "Shirley", "Larry", "Angela", "Jonathan", "Ruth", "Frank", "Brenda",
    "Scott", "Anna", "Justin", "Pamela", "Raymond", "Nicole", "Brandon", "Katherine", "Gregory", "Virginia",
    "Benjamin", "Deborah", "Samuel", "Rachel", "Patrick", "Catherine", "Jack", "Christine", "Dennis", "Samantha",
    "Jerry", "Debra", "Tyler", "Janet", "Aaron", "Carolyn", "Jose", "Allison", "Adam", "Heather"
]

last_names = [
    "Smith", "Johnson", "Williams", "Jones", "Brown", "Davis", "Miller", "Wilson", "Moore", "Taylor",
    "Anderson", "Thomas", "Jackson", "White", "Harris", "Martin", "Thompson", "Garcia", "Martinez", "Robinson",
    "Clark", "Rodriguez", "Lewis", "Lee", "Walker", "Hall", "Allen", "Young", "Hernandez", "King",
    "Wright", "Lopez", "Hill", "Scott", "Green", "Adams", "Baker", "Gonzalez", "Nelson", "Carter",
    "Mitchell", "Perez", "Roberts", "Turner", "Phillips", "Campbell", "Parker", "Evans", "Edwards", "Collins",
    "Stewart", "Sanchez", "Morris", "Rogers", "Reed", "Cook", "Morgan", "Bell", "Murphy", "Bailey",
    "Rivera", "Cooper", "Richardson", "Cox", "Howard", "Ward", "Torres", "Peterson", "Gray", "Ramirez",
    "James", "Watson", "Brooks", "Kelly", "Sanders", "Price", "Bennett", "Wood", "Barnes", "Ross",
    "Henderson", "Coleman", "Jenkins", "Perry", "Powell", "Long", "Patterson", "Hughes", "Flores", "Washington",
    "Butler", "Simmons", "Foster", "Gonzalez", "Bryant", "Alexander", "Russell", "Griffith", "Diaz", "Haynes"
]

lob_types = ['GL', 'PL', 'WC']

# Generate the SQL file
with open('insert_policies.sql', 'w') as f:
    f.write("INSERT INTO policies (prev_policy_id, customer_name, lob, coverage_start_date, coverage_end_date, cancellation_date_time) VALUES\n")

    for i in range(num_records):
        # Set prev_policy_id to NULL for most records, except every 20th record
        if i % 20 == 0 and i != 0:
            prev_policy_id = f"{i-1}"
        else:
            prev_policy_id = "NULL"

        first_name = random.choice(first_names)
        last_name = random.choice(last_names)
        customer_name = f"{first_name} {last_name}"
        lob = random.choice(lob_types)
        coverage_start_date = datetime.date(2023, 1, 1) + datetime.timedelta(days=i % 365)
        coverage_end_date = coverage_start_date + datetime.timedelta(days=365)

        # Generate a random cancellation date/time between coverage_start_date and coverage_end_date
        if random.random() < 0.1:  # Let's assume 10% of policies have a cancellation date
            random_days = random.randint(0, (coverage_end_date - coverage_start_date).days)
            cancellation_date = coverage_start_date + datetime.timedelta(days=random_days)
            cancellation_time = f'{random.randint(0, 23):02}:{random.randint(0, 59):02}:{random.randint(0, 59):02}'
            cancellation_date_time = f"'{cancellation_date} {cancellation_time}'"
        else:
            cancellation_date_time = "NULL"

        insert_statement = f"({prev_policy_id}, '{customer_name}', '{lob}', '{coverage_start_date}', '{coverage_end_date}', {cancellation_date_time})"

        if i < num_records - 1:
            insert_statement += ",\n"
        else:
            insert_statement += ";\n"

        f.write(insert_statement)
