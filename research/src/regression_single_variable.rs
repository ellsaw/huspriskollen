use std::{env, vec};

use dotenv::dotenv;
use plotly::common::{DashType, Line, Mode};
use plotly::layout::{Axis, Layout};
use plotly::{Plot, Scatter};
use postgres::{Client, Error, NoTls};

/*

    R^2 = 0.31279306194490836 on this dataset. i.e the single variable of home square footage in a linear regression explains 31.3% of the reason for the price

*/

struct PropertyObject {
    price: Vec<i32>,
    living_area_metres_squared: Vec<i32>,
    min_living_area: i32,
    max_living_area: i32,
}

impl PropertyObject {
    fn new() -> Self {
        PropertyObject {
            price: Vec::new(),
            living_area_metres_squared: Vec::new(),
            min_living_area: 0,
            max_living_area: 0,
        }
    }

    fn get_living_area_min_max(&mut self) {
        if self.living_area_metres_squared.is_empty() {
            panic!("PropertyObject must be filled before getting mix/max living area");
        }
        self.min_living_area = self
            .living_area_metres_squared
            .iter()
            .cloned()
            .min()
            .unwrap();
        self.max_living_area = self
            .living_area_metres_squared
            .iter()
            .cloned()
            .max()
            .unwrap();
    }
}

struct RegressionObject {
    mean_y: f64,
    x_values: Vec<f64>,
    y_values: Vec<f64>,
}

impl RegressionObject {
    fn new(properties: &PropertyObject) -> Self {
        let average_living_area: f64 = average(properties.living_area_metres_squared.clone());
        let average_price: f64 = average(properties.price.clone());

        let slope: f64 = {
            let mut dividend: Vec<f64> = Vec::new();
            let mut divider: Vec<f64> = Vec::new();

            for i in 0..properties.living_area_metres_squared.len() {
                dividend.push(
                    (properties.living_area_metres_squared[i] as f64 - average_living_area)
                        * (properties.price[i] as f64 - average_price),
                );
            }

            for i in 0..properties.living_area_metres_squared.len() {
                divider.push(
                    (properties.living_area_metres_squared[i] as f64 - average_living_area).powi(2),
                );
            }

            (dividend.iter().sum::<f64>()) / (divider.iter().sum::<f64>())
        };

        let intercept: f64 = average_price - (slope * average_living_area);

        let x_values: Vec<f64> = (properties.min_living_area..=properties.max_living_area)
            .map(|x| x as f64)
            .collect();

        let y_values: Vec<f64> = x_values.iter().map(|&x| slope * x + intercept).collect();

        RegressionObject {
            mean_y: average_price,
            x_values: x_values,
            y_values: y_values,
        }
    }
}

fn average(vector: Vec<i32>) -> f64 {
    let sum: i32 = vector.clone().iter().sum();

    return sum as f64 / vector.len() as f64;
}

fn fetch(ids: Vec<i32>) -> Result<PropertyObject, Error> {
    dotenv().ok();

    let postgres_url: String = env::var("POSTGRES_URL").expect("POSTGRES_URL must be set");

    let mut client: Client = Client::connect(&postgres_url, NoTls)?;

    let mut property_object: PropertyObject = PropertyObject::new();

    for id in ids {
        let row = client.query_one(
            "SELECT price, living_area_metres_squared FROM villa WHERE id = ($1)",
            &[&id],
        )?;

        property_object.price.push(row.get(0));

        property_object.living_area_metres_squared.push(row.get(1));
    }

    property_object.get_living_area_min_max();

    Ok(property_object)
}

fn r_squared(regression_object: &RegressionObject, properties: &PropertyObject) -> f64{
    let ssr: f64 = {
        let mut squares: Vec<f64> = Vec::new();

        for i in 0..properties.price.len() {

            if let Some(index) = regression_object
                .x_values
                .iter()
                .position(|&x| x == properties.living_area_metres_squared[i] as f64){

                let regression_y = regression_object.y_values[index];


                squares.push( (regression_y - regression_object.mean_y).powf(2.0) );

            } else{
                eprintln!("Error getting regression x index");
            };
        }

        squares.iter().sum::<f64>()
    };

    let sst: f64 = {
        let mut squares: Vec<f64> = Vec::new();

        for i in 0..properties.price.len(){
            squares.push( (properties.price[i] as f64 - regression_object.mean_y).powf(2.0) );
        }

        squares.iter().sum::<f64>()
    };

    return ssr / sst;

}

fn visualise(properties: PropertyObject) {
    let scatter_trace = Scatter::new(
        properties.living_area_metres_squared.clone(),
        properties.price.clone(),
    )
    .mode(Mode::Markers);

    let regression_object: RegressionObject = RegressionObject::new(&properties);

    println!("R^2: {}", r_squared(&regression_object, &properties));

    let line_of_best_fit = Scatter::new(regression_object.x_values, regression_object.y_values)
        .mode(Mode::Lines)
        .name("Line of best fit")
        .line(Line::new());

    let line_trace = Scatter::new(
        vec![properties.min_living_area, properties.max_living_area],
        vec![regression_object.mean_y, regression_object.mean_y],
    )
    .mode(Mode::Lines)
    .name("Mean price")
    .line(Line::new().dash(DashType::Dot));

    let layout = Layout::new()
        .title("Price / Living Area in Class")
        .x_axis(Axis::new().title("Living Area"))
        .y_axis(Axis::new().title("Price"));

    let mut plot = Plot::new();
    plot.add_trace(scatter_trace);
    plot.add_trace(line_of_best_fit);
    plot.add_trace(line_trace);
    plot.set_layout(layout);

    plot.show();
}

pub fn regression_single_variable(sample_ids:Vec<i32> ) {

    let properties: PropertyObject = match fetch(sample_ids) {
        Ok(res) => res,
        Err(error) => {
            eprintln!("Error fetching properties: {}", error);
            return;
        }
    };

    visualise(properties);
}
