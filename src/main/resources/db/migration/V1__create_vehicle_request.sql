CREATE TABLE vehicle_request (
  id            uuid PRIMARY KEY,
  business_key  text NOT NULL UNIQUE,
  status        text NOT NULL,
  purpose       text NOT NULL,
  created_at    timestamptz NOT NULL DEFAULT now(),
  CONSTRAINT vehicle_request_status_known
    CHECK (status IN ('DRAFT', 'SUBMITTED', 'APPROVED', 'ASSIGNED', 'COMPLETED', 'REJECTED'))
);
