use std::{env};

use dotenv::dotenv;
use postgres::{Client, Error, NoTls};

pub enum PropertyType {
    Villa,
}

struct Data {
    id: i32,
    euclidean_distance: f64,
}

pub fn k_nearest_neighbors(
    property_type: PropertyType,
    independent_latitude: f64,
    independent_longitude: f64,
    k: usize
) -> Result<Vec<i32>, Error> {

    let table: &str = {
        match property_type {
            PropertyType::Villa => "villa",
        }
    };

    let mut dataset: Vec<Data> = Vec::new();

    dotenv().ok();

    let postgres_url: String = env::var("POSTGRES_URL").expect("POSTGRES_URL must be set");

    let mut client: Client = Client::connect(&postgres_url, NoTls)?;

    for row in client.query(
        &format!("SELECT id, latitude, longitude FROM {}", table),
        &[],
    )? {
        let id: i32 = row.get(0);
        let dependent_latitude: f64 = row.get(1);
        let dependent_longitude: f64 = row.get(2);

        let euclidean_distance: f64 = ((independent_longitude - dependent_longitude).powi(2)
            + (independent_latitude - dependent_latitude).powi(2))
        .sqrt();

        dataset.push(Data { id: id, euclidean_distance: euclidean_distance });
    }

    dataset.sort_by(|a, b| a.euclidean_distance.partial_cmp(&b.euclidean_distance).unwrap());

    dataset.truncate(k);

    let mut result: Vec<i32> = Vec::new();

    for data in dataset{
        result.push(data.id);
    };

    Ok(result)
}
