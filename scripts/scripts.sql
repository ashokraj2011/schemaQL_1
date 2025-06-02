-- public.accounts definition

-- Drop table

-- DROP TABLE public.accounts;

CREATE TABLE public.accounts (
                                 customer_id int4 NOT NULL,
                                 accountnum text NOT NULL,
                                 balance numeric(10, 2) NOT NULL,
                                 account_type text NOT NULL,
                                 created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
                                 CONSTRAINT accounts_pkey PRIMARY KEY (customer_id)
);


-- public.customers definition

-- Drop table

-- DROP TABLE public.customers;

CREATE TABLE public.customers (
                                  customer_id int4 NOT NULL,
                                  first_name text NOT NULL,
                                  email text NULL,
                                  profile_data jsonb NULL,
                                  last_name text NULL,
                                  CONSTRAINT customers_pkey PRIMARY KEY (customer_id)
);
INSERT INTO public.accounts (customer_id,accountnum,balance,account_type,created_at) VALUES
INSERT INTO public.customers (customer_id,first_name,email,profile_data,last_name) VALUES
    (1,'Alice','alice@example.com','{"contact": {"phone": "9876543210"}}','Smith'),
    (2,'Bob','bob@example.com','{"contact": {"phone": "9123456789"}}','Smith'),
    (3,'Charlie',NULL,'{"contact": {"phone": "9999999999"}}','Smith');                                                                                    (1,'ACC123456',2500.75,'savings','2025-04-20 15:32:25.706'),
                                                                                         (2,'ACC789012',5200.00,'checking','2025-04-20 15:32:25.706'),
                                                                                         (3,'ACC345678',180.50,'savings','2025-04-20 15:32:25.706'),
                                                                                         (4,'ACC901234',9999.99,'business','2025-04-20 15:32:25.706'),
                                                                                         (5,'ACC567890',0.00,'checking','2025-04-20 15:32:25.706');