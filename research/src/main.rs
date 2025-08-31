use std::{env, vec};

use data_visualisation::regression_multiple_variables::{regression_multiple_variables};
use data_visualisation::regression_single_variable::{regression_single_variable};
use dotenv::dotenv;
use plotly::common::{Marker, Mode};
use plotly::layout::{Axis, Layout};
use plotly::{Plot, Scatter};
use postgres::{Client, Error, NoTls};

struct CoordinatesObject {
    latitudes: Vec<f64>,
    longitudes: Vec<f64>,
}

impl CoordinatesObject {
    fn new() -> Self {
        CoordinatesObject {
            latitudes: Vec::new(),
            longitudes: Vec::new()
        }
    }
}

fn get_coordinates() -> Result<CoordinatesObject, Error> {
    dotenv().ok();

    let postgres_url: String =
        env::var("POSTGRES_URL").expect("POSTGRES_URL must be set");

    let mut client: Client = Client::connect(
        &postgres_url,
        NoTls,
    )?;

    let mut coordinates_object: CoordinatesObject = CoordinatesObject::new();

    for row in client.query("SELECT latitude, longitude FROM villa", &[])? {
        coordinates_object.latitudes.push(row.get(0));
        coordinates_object.longitudes.push(row.get(1));
    }

    Ok(coordinates_object)
}

fn visualise_locations() -> () {
    let coordinates_object: CoordinatesObject = match get_coordinates() {
        Ok(result) => result,
        Err(e) => {
            eprintln!("Failed to get coordinates: {}", e);
            return;
        }
    };

    let trace = Scatter::new(coordinates_object.longitudes, coordinates_object.latitudes)
        .mode(Mode::Markers)
        .marker(Marker::new().size(2));

    let layout = Layout::new()
        .width(400)
        .height(700)
        .title("Coordinates for all houses in the training set")
        .x_axis(Axis::new().title("Longitude").range(vec![11.0270, 24.1450]).scale_anchor("x"))
        .y_axis(Axis::new().title("Latitude").range(vec![55.3419, 69.0597]));

    let mut plot = Plot::new();
    plot.add_trace(trace);
    plot.set_layout(layout);

    plot.show();
}

fn main() {
    let sample_ids: Vec<i32> = vec![
        621, 713, 1677, 2318, 2162, 1223, 375, 1130, 2023, 2172, 1667, 966, 2, 202, 1472, 624,
        2054, 914, 1951, 156, 865, 1559, 2158, 1309, 1660, 1670, 426, 2153, 1035, 1908, 483,
    ];

    /* visualise_locations(); */
    /* regression_single_variable(sample_ids); */
    regression_multiple_variables(sample_ids);
}
